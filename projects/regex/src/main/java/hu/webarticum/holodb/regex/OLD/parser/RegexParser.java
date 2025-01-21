package hu.webarticum.holodb.regex.OLD.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hu.webarticum.holodb.regex.OLD.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.OLD.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.OLD.ast.AstNode;
import hu.webarticum.holodb.regex.OLD.ast.BackreferenceAstNode;
import hu.webarticum.holodb.regex.OLD.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.OLD.ast.CharacterClassAstNode;
import hu.webarticum.holodb.regex.OLD.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.OLD.ast.CharacterMatchAstNode;
import hu.webarticum.holodb.regex.OLD.ast.FixedStringAstNode;
import hu.webarticum.holodb.regex.OLD.ast.GroupAstNode;
import hu.webarticum.holodb.regex.OLD.ast.LinebreakAstNode;
import hu.webarticum.holodb.regex.OLD.ast.NamedBackreferenceAstNode;
import hu.webarticum.holodb.regex.OLD.ast.PosixCharacterClassAstNode;
import hu.webarticum.holodb.regex.OLD.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.OLD.ast.RangeAstNode;
import hu.webarticum.holodb.regex.OLD.ast.SequenceAstNode;
import hu.webarticum.holodb.regex.OLD.ast.UnicodePropertyCharacterClassAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class RegexParser {
    
    public AstNode parse(String patternString) {
        ParserInput parserInput = new ParserInput(patternString);
        AstNode result = parseAlternation(parserInput);
        if (parserInput.hasNext()) {
            int position = parserInput.position();
            char next = parserInput.next();
            throw error(parserInput, position, "Unexpected input '" + next + "'");
        }
        return result;
    }
    
    private AlternationAstNode parseAlternation(ParserInput parserInput) {
        List<SequenceAstNode> branches = new ArrayList<>(1);
        branches.add(parseSequence(parserInput));
        while (parserInput.expect('|')) {
            branches.add(parseSequence(parserInput));
        }
        return AlternationAstNode.of(ImmutableList.fromCollection(branches));
    }

    private SequenceAstNode parseSequence(ParserInput parserInput) {
        requireNonQuantifier(parserInput);
        List<AstNode> items = new ArrayList<>();
        AstNode nextInSequence;
        while ((nextInSequence = parseNextInSequence(parserInput)) != null) {
            int[] quantifierData = parseQuantifier(parserInput);
            AstNode nextNode = (quantifierData != null ?
                    QuantifiedAstNode.of(nextInSequence, quantifierData[0], quantifierData[1]):
                    nextInSequence);
            items.add(nextNode);
            requireNonQuantifier(parserInput);
        }
        return SequenceAstNode.of(ImmutableList.fromCollection(items));
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
            return parseOpenedBracketCharacterClass(parserInput);
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
        if (!parserInput.expect(')')) {
            throw error(parserInput, parserInput.position(), "Unexpected end of group");
        }
        return GroupAstNode.of(alternation, kind, name);
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
                throw error(parserInput, nextNextPosition, "Unexpected input '" + nextNextNext + "' inside P group");
            }
            String name = parseOpenedName(parserInput, '>');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else if (nextNext == '<') {
            String name = parseOpenedName(parserInput, '>');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else if (nextNext == '\'') {
            String name = parseOpenedName(parserInput, '\'');
            return new Object[] { GroupAstNode.Kind.NAMED, name };
        } else {
            int nextPosition = parserInput.position() - 1;
            throw error(parserInput, nextPosition, "Unsupported special group modifier '" + nextNext + "'");
        }
    }

    private void requireNonQuantifier(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            return;
        }
        char next = parserInput.peek();
        if (next == '?' || next == '*' || next == '+' || next == '{') {
            int position = parserInput.position();
            throw error(parserInput, position, "Unexpected quantifer '" + next + "'");
        }
    }
    
    private AstNode parseOpenedEscapeSequence(ParserInput parserInput) {
        requireNonEnd(parserInput);
        int position = parserInput.position() - 1;
        char next = parserInput.next();
        if (next == '0') {
            int number = parseOctalNumber(parserInput);
            return CharacterConstantAstNode.of((char) number);
        } else if (next >= '1' && next <= '9') {
            parserInput.storno();
            int number = parseDecimalNumber(parserInput);
            return BackreferenceAstNode.of(number);
        }
        if (!Character.isAlphabetic(next)) {
            return CharacterConstantAstNode.of(next);
        }
        switch (next) {
            case 'b':
                return AnchorAstNode.WORD_BOUNDARY;
            case 'B':
                return AnchorAstNode.NON_WORD_BOUNDARY;
            case 'A':
                return AnchorAstNode.BEGIN_OF_INPUT;
            case 'z':
                return AnchorAstNode.END_OF_INPUT;
            case 'Z':
                return AnchorAstNode.END_OF_INPUT_ALLOW_NEWLINE;
            case 'G':
                return AnchorAstNode.END_OF_PREVIOUS_MATCH;
            case 'w':
                return BuiltinCharacterClassAstNode.WORD;
            case 'W':
                return BuiltinCharacterClassAstNode.NON_WORD;
            case 'd':
                return BuiltinCharacterClassAstNode.DIGIT;
            case 'D':
                return BuiltinCharacterClassAstNode.NON_DIGIT;
            case 's':
                return BuiltinCharacterClassAstNode.WHITESPACE;
            case 'S':
                return BuiltinCharacterClassAstNode.NON_WHITESPACE;
            case 'h':
                return BuiltinCharacterClassAstNode.HORIZONTAL_WHITESPACE;
            case 'H':
                return BuiltinCharacterClassAstNode.NON_HORIZONTAL_WHITESPACE;
            case 'v':
                return BuiltinCharacterClassAstNode.VERTICAL_WHITESPACE;
            case 'V':
                return BuiltinCharacterClassAstNode.NON_VERTICAL_WHITESPACE;
            case 'R':
                return LinebreakAstNode.instance();
            case 'n':
                return CharacterConstantAstNode.of('\n');
            case 'r':
                return CharacterConstantAstNode.of('\r');
            case 't':
                return CharacterConstantAstNode.of('\t');
            case 'f':
                return CharacterConstantAstNode.of('\f');
            case 'a':
                return CharacterConstantAstNode.of('\u0007');
            case 'e':
                return CharacterConstantAstNode.of('\u001B');
            case 'x':
                return parseOpenedHexadecimalEscapeSequence(parserInput, 2);
            case 'u':
                return parseOpenedHexadecimalEscapeSequence(parserInput, 4);
            case 'c':
                return parseOpenedControlCharacterEscapeSequence(parserInput);
            case 'k':
                return parseOpenedNamedBackreference(parserInput);
            case 'p':
            case 'P':
                return parseOpenedProperyCharacterClass(parserInput, next == 'p');
            case 'Q':
                return parseOpenedQuotedString(parserInput);
            default:
                throw error(parserInput, position, "Unsupported escape sequence '\\" + next + "'");
        }
        
    }
    
    private CharacterConstantAstNode parseOpenedHexadecimalEscapeSequence(ParserInput parserInput, int unbracedLength) {
        requireNonEnd(parserInput);
        int codePoint;
        if (parserInput.expect('{')) {
            codePoint = parseHexadecimalNumber(parserInput);
            if (codePoint == -1) {
                int position = parserInput.position() - 3;
                throw error(parserInput, position, "Invalid hexadecimal escape sequence");
            }
            requireNonEnd(parserInput);
            if (parserInput.next() != '}') {
                int trailingPosition = parserInput.position() - 1;
                throw error(parserInput, trailingPosition, "Unexpected end of braced hexadecimal escape sequence");
            }
        } else {
            codePoint = parseHexadecimalNumber(parserInput, unbracedLength);
        }
        return CharacterConstantAstNode.of((char) codePoint);
    }

    private CharacterConstantAstNode parseOpenedControlCharacterEscapeSequence(ParserInput parserInput) {
        requireNonEnd(parserInput);
        char next = parserInput.next();
        if ((next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z')) {
            int codePoint = next & 31;
            return CharacterConstantAstNode.of((char) codePoint);
        }
        int namePosition = parserInput.position() - 1;
        switch (next) {
            case '@':
                return CharacterConstantAstNode.of((char) 0);
            case '[':
                return CharacterConstantAstNode.of((char) 27);
            case '\\':
                return CharacterConstantAstNode.of((char) 28);
            case ']':
                return CharacterConstantAstNode.of((char) 29);
            case '^':
                return CharacterConstantAstNode.of((char) 30);
            case '_':
                return CharacterConstantAstNode.of((char) 31);
            case '?':
                return CharacterConstantAstNode.of((char) 127);
            default:
                throw error(parserInput, namePosition, "Invalid control character name '" + next + "'");
        }
    }
    
    private NamedBackreferenceAstNode parseOpenedNamedBackreference(ParserInput parserInput) {
        requireNonEnd(parserInput);
        int ltPosition = parserInput.position();
        char ltChar = parserInput.next();
        if (ltChar != '<') {
            throw error(parserInput, ltPosition, "Unexpected character " + ltChar + " instead of backreference name");
        }
        String groupName = parseOpenedName(parserInput, '>');
        return NamedBackreferenceAstNode.of(groupName);
    }

    private CharacterMatchAstNode parseOpenedProperyCharacterClass(ParserInput parserInput, boolean positive) {
        requireNonEnd(parserInput);
        char next = parserInput.next();
        if (next != '{') {
            int bracePosition = parserInput.position() - 1;
            throw error(parserInput, bracePosition, "Unexpected character " + next + " instead of opening brace");
        }
        int namePosition = parserInput.position();
        String propertyName = parseOpenedName(parserInput, '}');
        Optional<? extends CharacterMatchAstNode> optional = PosixCharacterClassAstNode.of(propertyName, positive);
        if (optional.isPresent()) {
            return optional.get();
        }
        optional = UnicodePropertyCharacterClassAstNode.of(propertyName, positive);
        if (optional.isPresent()) {
            return optional.get();
        }
        throw error(parserInput, namePosition, "Unknown property name '" + propertyName + "'");
    }

    private FixedStringAstNode parseOpenedQuotedString(ParserInput parserInput) {
        requireNonEnd(parserInput);
        StringBuilder fixedStringBuilder = new StringBuilder();
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (next == '\\' && parserInput.expect('E')) {
                break;
            }
            fixedStringBuilder.append(next);
        }
        String fixedString = fixedStringBuilder.toString();
        return FixedStringAstNode.of(fixedString);
    }

    private CharacterClassAstNode parseOpenedBracketCharacterClass( // NOSONAR intentional complexity
            ParserInput parserInput) {
        requireNonEnd(parserInput);
        boolean positive = !parserInput.expect('^');
        List<CharacterMatchAstNode> nodes = new ArrayList<>();
        while (true) { // NOSONAR intentional complexity
            requireNonEnd(parserInput);
            char next = parserInput.next();
            if (next == '-') {
                boolean isFirst = nodes.isEmpty();
                if (isFirst) {
                    nodes.add(CharacterConstantAstNode.of(next));
                } else {
                    char nextNext = parserInput.next();
                    if (nextNext == ']') {
                        nodes.add(CharacterConstantAstNode.of(next));
                        break;
                    } else {
                        int lastIndex = nodes.size() - 1;
                        CharacterMatchAstNode lastNode = nodes.get(lastIndex);
                        if (!(lastNode instanceof CharacterConstantAstNode)) {
                            nodes.add(CharacterConstantAstNode.of(next));
                            continue;
                        }
                        CharacterMatchAstNode nextNode = parseNextInCharacterClass(parserInput, nextNext);
                        if (!(nextNode instanceof CharacterConstantAstNode)) {
                            nodes.add(CharacterConstantAstNode.of(next));
                            nodes.add(nextNode);
                            continue;
                        }
                        char lowChar = ((CharacterConstantAstNode) lastNode).value();
                        char highChar = ((CharacterConstantAstNode) nextNode).value();
                        nodes.set(lastIndex, RangeAstNode.of(lowChar, highChar));
                    }
                }
            } else if (next == ']') {
                if (nodes.isEmpty()) {
                    nodes.add(CharacterConstantAstNode.of(next));
                } else {
                    break;
                }
            } else {
                nodes.add(parseNextInCharacterClass(parserInput, next));
            }
        }
        return CharacterClassAstNode.of(positive, ImmutableList.fromCollection(nodes));
    }
    
    private CharacterMatchAstNode parseNextInCharacterClass(ParserInput parserInput, char firstChar) {
        if (firstChar == '\\') {
            int position = parserInput.position() - 1;
            AstNode escapedNode = parseOpenedEscapeSequence(parserInput);
            if (!(escapedNode instanceof CharacterMatchAstNode)) {
                throw error(parserInput, position, "Invalid escape sequence type inside character class");
            }
            return (CharacterMatchAstNode) escapedNode;
        } else if (firstChar == '[') {
            return parseOpenedBracketCharacterClass(parserInput);
        } else {
            return CharacterConstantAstNode.of(firstChar);
        }
    }
    
    private AstNode parseSingleInputCharacter(char next) {
        if (next == '.') {
            return BuiltinCharacterClassAstNode.ANY;
        } else if (next == '^') {
            return AnchorAstNode.BEGIN_OF_LINE;
        } else if (next == '$') {
            return AnchorAstNode.END_OF_LINE;
        } else {
            return CharacterConstantAstNode.of(next);
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
            result = parseOpenedBracedQuantifier(parserInput);
        } else {
            parserInput.storno();
            return null; // NOSONAR
        }
        if (parserInput.hasNext()) {
            char nextNext = parserInput.peek();
            if (nextNext == '?') {
                throw error(parserInput, position, "Lazy quantifiers are not supported");
            } else if (nextNext == '+') {
                throw error(parserInput, position, "Possessive quantifiers are not supported");
            }
        }
        return result;
    }
    
    private int[] parseOpenedBracedQuantifier(ParserInput parserInput) {
        int position = parserInput.position();
        int num1 = parseDecimalNumber(parserInput);
        if (num1 == -1 || !parserInput.hasNext()) {
            throw error(parserInput, position, "Invalid initial number in braced quantifier");
        }
        int afterNum1 = parserInput.next();
        if (afterNum1 == '}') {
            return new int[] { num1, num1 };
        } else if (afterNum1 == ',') {
            int num2Candidate = parseDecimalNumber(parserInput);
            if (!parserInput.hasNext() || parserInput.next() != '}') {
                throw error(parserInput, position, "Incomplete two-component braced quantifier");
            }
            int num2 = (num2Candidate == -1) ? QuantifiedAstNode.NO_UPPER_LIMIT : num2Candidate;
            return new int[] { num1, num2 };
        } else {
            throw error(parserInput, position, "Incomplete single-component braced quantifier");
        }
    }

    private String parseOpenedName(ParserInput parserInput, char expectedEnd) {
        StringBuilder nameBuilder = new StringBuilder();
        requireNonEnd(parserInput);
        char first = parserInput.next();
        if (!isAllowedNameFirstChar(first)) {
            int firstPosition = parserInput.position() - 1;
            throw error(parserInput, firstPosition, "Invalid name first character '" + first + "'");
        }
        nameBuilder.append(first);
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (next == expectedEnd) {
                return nameBuilder.toString();
            } else if (!isAllowedNameFurtherChar(next)) {
                int nextPosition = parserInput.position() - 1;
                throw error(parserInput, nextPosition, "Invalid name further character '" + next + "'");
            }
            nameBuilder.append(next);
        }
        throw error(parserInput, parserInput.position(), "Unexpected end after name sequence");
    }
    
    private boolean isAllowedNameFirstChar(char c) {
        return (
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_');
    }

    private boolean isAllowedNameFurtherChar(char c) {
        return (
                isAllowedNameFirstChar(c) ||
                (c >= '0' && c <= '9'));
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

    private int parseHexadecimalNumber(ParserInput parserInput) {
        StringBuilder digits = new StringBuilder();
        while (parserInput.hasNext()) {
            char next = parserInput.next();
            if (
                    (next >= '0' && next <= '9') ||
                    (next >= 'A' && next <= 'F') ||
                    (next >= 'a' && next <= 'f')) {
                digits.append(next);
            } else {
                parserInput.storno();
                break;
            }
        }
        if (digits.length() == 0) {
            return -1;
        } else {
            return Integer.parseInt(digits.toString(), 16);
        }
    }

    private int parseHexadecimalNumber(ParserInput parserInput, int length) {
        int numberPosition = parserInput.position();
        StringBuilder hexadecimalStringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            requireNonEnd(parserInput);
            hexadecimalStringBuilder.append(parserInput.next());
        }
        String hexadecimalLiteral = hexadecimalStringBuilder.toString();
        try {
            return Integer.parseInt(hexadecimalLiteral, 16);
        } catch (NumberFormatException e) {
            throw error(parserInput, numberPosition, "Invalid hexadecimal literal '" + hexadecimalLiteral + "'", e);
        }
    }

    private void requireNonEnd(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            int position = parserInput.position();
            throw error(parserInput, position, "Unexpected end");
        }
    }

    private RegexParserException error(ParserInput parserInput, int position, String description) {
        return error(parserInput, position, description, null);
    }
    
    private RegexParserException error(ParserInput parserInput, int position, String description, Exception e) {
        String message = description + " at position " + position + " in pattern " + parserInput.content();
        return new RegexParserException(position, message, e);
    }
    
}
