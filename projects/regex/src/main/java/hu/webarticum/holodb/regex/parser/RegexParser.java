package hu.webarticum.holodb.regex.parser;

import java.util.ArrayList;
import java.util.List;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.BackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.LinebreakAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class RegexParser {
    
    public AstNode parse(String patternString) {
        ParserInput parserInput = new ParserInput(patternString);
        AstNode result = parseAlternation(parserInput);
        if (parserInput.hasNext()) {
            int position = parserInput.position();
            char next = parserInput.next();
            throw new RegexParserException(position, "Unexpected input at position " + position + ": " + next);
        }
        return result;
    }
    
    private AlternationAstNode parseAlternation(ParserInput parserInput) {
        List<SequenceAstNode> branches = new ArrayList<>(1);
        branches.add(parseSequence(parserInput));
        while (parserInput.hasNext()) {
            if (parserInput.next() == '|') {
                branches.add(parseSequence(parserInput));
            } else {
                parserInput.storno();
                break;
            }
        }
        return new AlternationAstNode(ImmutableList.fromCollection(branches));
    }

    private SequenceAstNode parseSequence(ParserInput parserInput) {
        requireNonQuantifier(parserInput);
        List<AstNode> items = new ArrayList<>();
        AstNode nextInSequence;
        while ((nextInSequence = parseNextInSequence(parserInput)) != null) {
            int[] quantifierData = parseQuantifier(parserInput);
            AstNode nextNode = (quantifierData != null ?
                    new QuantifiedAstNode(nextInSequence, quantifierData[0], quantifierData[1]):
                    nextInSequence);
            items.add(nextNode);
            requireNonQuantifier(parserInput);
        }
        return new SequenceAstNode(ImmutableList.fromCollection(items));
    }

    private AstNode parseNextInSequence(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            return null;
        }
        char next = parserInput.next();
        if (next == '|' || next == ')') {
            parserInput.storno();
            return null;
        } else if (next == '(') {
            return parseOpenedGroup(parserInput);
        } else if (next == '\\') {
            return parseOpenedEscapeSequence(parserInput);
        } else if (next == '[') {
            return parseOpenedCharacterClass(parserInput);
        } else {
            return parseSingleInputCharacter(next);
        }
    }
    
    private GroupAstNode parseOpenedGroup(ParserInput parserInput) {
        requireNonEnd(parserInput);
        Object[] groupMetadata = parseGroupPrefix(parserInput);
        GroupAstNode.Kind kind = (GroupAstNode.Kind) groupMetadata[0];
        String name = (String) groupMetadata[1];
        AlternationAstNode alternation = parseAlternation(parserInput);
        requireNonEnd(parserInput);
        int afterPosition = parserInput.position();
        char after = parserInput.next();
        if (after != ')') {
            throw new RegexParserException(
                    afterPosition,
                    "Unexpected input at position " + afterPosition + ": " + after);
        }
        return new GroupAstNode(alternation, kind, name);
    }
    
    private Object[] parseGroupPrefix(ParserInput parserInput) {
        char next = parserInput.next();
        if (next != '?') {
            parserInput.storno();
            return new Object[] { GroupAstNode.Kind.CAPTURING, "" };
        }
        requireNonEnd(parserInput);
        char nextNext = parserInput.next();
        if (nextNext == ':') {
            return new Object[] { GroupAstNode.Kind.NON_CAPTURING, "" };
        } else if (nextNext == 'P') {
            requireNonEnd(parserInput);
            char nextNextNext = parserInput.next();
            if (nextNextNext != '<') {
                int nextNextPosition = parserInput.position() - 1;
                throw new RegexParserException(
                        nextNextPosition,
                        "Unexpected input inside P group at position " + nextNextPosition + ": " + nextNextNext);
            }
            String name = parseOpenedGroupName(parserInput, '>');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else if (nextNext == '<') {
            String name = parseOpenedGroupName(parserInput, '>');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else if (nextNext == '\'') {
            String name = parseOpenedGroupName(parserInput, '\'');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else {
            int nextPosition = parserInput.position() - 1;
            throw new RegexParserException(
                    nextPosition,
                    "Unsupported special group modifier at position " + nextPosition + ": " + nextNext);
        }
    }

    private String parseOpenedGroupName(ParserInput parserInput, char expectedEnd) {
        StringBuilder nameBuilder = new StringBuilder();
        requireNonEnd(parserInput);
        char first = parserInput.next();
        if (!isAllowedGroupNameFirstChar(first)) {
            int firstPosition = parserInput.position() - 1;
            throw new RegexParserException(
                    firstPosition,
                    "Invalid group name first character at position " + firstPosition + ": " + first);
        }
        nameBuilder.append(first);
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (next == expectedEnd) {
                return nameBuilder.toString();
            } else if (!isAllowedGroupNameFurtherChar(next)) {
                int nextPosition = parserInput.position() - 1;
                throw new RegexParserException(
                        nextPosition,
                        "Invalid group name further character at position " + nextPosition + ": " + first);
            }
            nameBuilder.append(next);
        }
        throw unexpectedEnd(parserInput.position());
    }
    
    private boolean isAllowedGroupNameFirstChar(char c) {
        return (
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_');
    }

    private boolean isAllowedGroupNameFurtherChar(char c) {
        return (
                isAllowedGroupNameFirstChar(c) ||
                (c >= '0' && c <= '9'));
    }
    
    private void requireNonQuantifier(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            return;
        }
        char next = parserInput.peek();
        if (next == '?' || next == '*' || next == '+' || next == '{') {
            int position = parserInput.position();
            throw new RegexParserException(position, "Unexpected quantifer at position " + position + ": " + next);
        }
    }
    
    private AstNode parseOpenedEscapeSequence(ParserInput parserInput) {
        requireNonEnd(parserInput);
        int position = parserInput.position() - 1;
        char next = parserInput.next();
        if (next == '0') {
            int number = parseOctalNumber(parserInput);
            return new CharacterLiteralAstNode((char) number);
        } else if (next >= '1' && next <= '9') {
            parserInput.storno();
            int number = parseDecimalNumber(parserInput);
            return new BackreferenceAstNode(number);
        }
        if (!Character.isAlphabetic(next)) {
            return new CharacterLiteralAstNode(next);
        }
        switch (next) {
            case 'R':
                return new LinebreakAstNode();
            case 'b':
                return new AnchorAstNode(AnchorAstNode.Kind.WORD_BOUNDARY);
            case 'B':
                return new AnchorAstNode(AnchorAstNode.Kind.NON_WORD_BOUNDARY);
            case 'A':
                return new AnchorAstNode(AnchorAstNode.Kind.BEGIN_OF_INPUT);
            case 'z':
                return new AnchorAstNode(AnchorAstNode.Kind.END_OF_INPUT);
            case 'Z':
                return new AnchorAstNode(AnchorAstNode.Kind.END_OF_INPUT_ALLOW_NEWLINE);
            case 'G':
                return new AnchorAstNode(AnchorAstNode.Kind.END_OF_PREVIOUS_MATCH);
            case 'w':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.WORD);
            case 'W':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.NON_WORD);
            case 'd':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.DIGIT);
            case 'D':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.NON_DIGIT);
            case 's':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.WHITESPACE);
            case 'S':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.NON_WHITESPACE);
            case 'h':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.HORIZONTAL_WHITESPACE);
            case 'H':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.NON_HORIZONTAL_WHITESPACE);
            case 'v':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.VERTICAL_WHITESPACE);
            case 'V':
                return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.NON_VERTICAL_WHITESPACE);
            default:
                throw new RegexParserException(
                        position, "Unsupported escape sequence \\" + next + " at position: " + position);
        }
        
    }
    
    private AstNode parseOpenedCharacterClass(ParserInput parserInput) {
        requireNonEnd(parserInput);
        
        // TODO
        throw new UnsupportedOperationException("Character class: not implemented yet");
        
    }
    
    private AstNode parseSingleInputCharacter(char next) {
        if (next == '.') {
            return new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.ANY);
        } else if (next == '^') {
            return new AnchorAstNode(AnchorAstNode.Kind.BEGIN_OF_LINE);
        } else if (next == '$') {
            return new AnchorAstNode(AnchorAstNode.Kind.END_OF_LINE);
        } else {
            return new CharacterLiteralAstNode(next);
        }
    }
    
    private int[] parseQuantifier(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            return null; // NOSONAR
        }
        int position = parserInput.position();
        char next = parserInput.next();
        int[] result;
        if (next == '?') {
            result = new int[] { 0, 1 };
        } else if (next == '*') {
            result = new int[] { 0, QuantifiedAstNode.NO_UPPER_LIMIT };
        } else if (next == '+') {
            result = new int[] { 1, QuantifiedAstNode.NO_UPPER_LIMIT };
        } else if (next == '{') {
            result = parseOpenedCurlyQuantifier(parserInput);
        } else {
            parserInput.storno();
            return null; // NOSONAR
        }
        if (parserInput.hasNext()) {
            char nextNext = parserInput.peek();
            if (nextNext == '?') {
                throw new RegexParserException(
                        position, "Lazy quantifiers are not supported, used at position " + position);
            } else if (nextNext == '+') {
                throw new RegexParserException(
                        position, "Possessive quantifiers are not supported, used at position " + position);
            }
        }
        return result;
    }
    
    private int[] parseOpenedCurlyQuantifier(ParserInput parserInput) {
        int position = parserInput.position();
        int num1 = parseDecimalNumber(parserInput);
        if (num1 == -1 || !parserInput.hasNext()) {
            throw new RegexParserException(position, "Invalid quantifier at position " + position);
        }
        int afterNum1 = parserInput.next();
        if (afterNum1 == '}') {
            return new int[] { num1, num1 };
        } else if (afterNum1 == ',') {
            int num2Candidate = parseDecimalNumber(parserInput);
            if (!parserInput.hasNext() || parserInput.next() != '}') {
                throw new RegexParserException(position, "Invalid quantifier at position " + position);
            }
            int num2 = (num2Candidate == -1) ? QuantifiedAstNode.NO_UPPER_LIMIT : num2Candidate;
            return new int[] { num1, num2 };
        } else {
            throw new RegexParserException(position, "Invalid quantifier at position " + position);
        }
    }
    
    private int parseDecimalNumber(ParserInput parserInput) {
        StringBuilder digits = new StringBuilder();
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (next >= '0' && next <= '9') {
                digits.append(next);
            } else {
                parserInput.storno();
                break;
            }
        }
        if (digits.length() == 0) {
            return -1;
        } else {
            return Integer.parseInt(digits.toString());
        }
    }

    private int parseOctalNumber(ParserInput parserInput) {
        StringBuilder digits = new StringBuilder();
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (next >= '0' && next <= '7') {
                digits.append(next);
            } else {
                parserInput.storno();
                break;
            }
        }
        if (digits.length() == 0) {
            return -1;
        } else {
            return Integer.parseInt(digits.toString(), 8);
        }
    }

    private void requireNonEnd(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            throw unexpectedEnd(parserInput.position());
        }
    }
    
    private RegexParserException unexpectedEnd(int position) {
        return new RegexParserException(position, "Unexpected end at position " + position);
    }

}
