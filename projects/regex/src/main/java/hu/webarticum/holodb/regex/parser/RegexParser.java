package hu.webarticum.holodb.regex.parser;

import java.util.ArrayList;
import java.util.List;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
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
        int position = parserInput.position();
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
        return new AlternationAstNode(position, ImmutableList.fromCollection(branches));
    }

    private SequenceAstNode parseSequence(ParserInput parserInput) {
        int position = parserInput.position();
        List<AstNode> items = new ArrayList<>();
        AstNode nextInSequence;
        while ((nextInSequence = parseNextInSequence(parserInput)) != null) {
            items.add(nextInSequence);
        }
        return new SequenceAstNode(position, ImmutableList.fromCollection(items));
    }

    // TODO: simple implementation
    private AstNode parseNextInSequence(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            return null;
        }
        int position = parserInput.position();
        char next = parserInput.next();
        if (next == '|' || next == ')') {
            parserInput.storno();
            return null;
        }
        if (next == '(') {
            return parseOpenedGroup(parserInput);
        }
        
        // FIXME: dummy implementation
        if (Character.isAlphabetic(next) || Character.isDigit(next)) {
            return new CharacterLiteralAstNode(position, next);
        } else {
            throw new RegexParserException(position, "Invalid input at position " + position + ": " + next);
        }
    }
    
    private GroupAstNode parseOpenedGroup(ParserInput parserInput) {
        requireNonEnd(parserInput);
        int startPosition = parserInput.position() - 1;
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
        return new GroupAstNode(startPosition, alternation, kind, name);
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
    
    private void requireNonEnd(ParserInput parserInput) {
        if (!parserInput.hasNext()) {
            throw unexpectedEnd(parserInput.position());
        }
    }
    
    private RegexParserException unexpectedEnd(int position) {
        return new RegexParserException(position, "Unexpected end at position " + position);
    }

}
