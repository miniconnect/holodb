package hu.webarticum.holodb.regex.parser;

import java.util.ArrayList;
import java.util.List;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class RegexParser {

    public AstNode parse(String patternString) {
        ParserInput parserInput = new ParserInput(patternString);
        AstNode result = parseAlternation(parserInput);
        if (parserInput.hasNext()) {
            int position = parserInput.position();
            char next = parserInput.next();
            throw new RegexParserException(position, "Unexpected input '" + next + "' at position " + position);
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
        
        // FIXME: dummy implementation
        if (Character.isAlphabetic(next) || Character.isDigit(next)) {
            return new CharacterLiteralAstNode(position, next);
        } else {
            throw new IllegalArgumentException("Invalid input at position " + position + ": " + next);
        }
    }

}
