package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class RegexParserTest {

    @Test
    void testEmpty() {
        String pattern = "";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.empty())
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleCharacters() {
        String pattern = "abc123";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new CharacterLiteralAstNode(0, 'a'),
                new CharacterLiteralAstNode(1, 'b'),
                new CharacterLiteralAstNode(2, 'c'),
                new CharacterLiteralAstNode(3, '1'),
                new CharacterLiteralAstNode(4, '2'),
                new CharacterLiteralAstNode(5, '3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleAlternation() {
        String pattern = "a1|b2|c333";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new CharacterLiteralAstNode(0, 'a'),
                new CharacterLiteralAstNode(1, '1')
            )),
            new SequenceAstNode(3, ImmutableList.of(
                new CharacterLiteralAstNode(3, 'b'),
                new CharacterLiteralAstNode(4, '2')
            )),
            new SequenceAstNode(6, ImmutableList.of(
                new CharacterLiteralAstNode(6, 'c'),
                new CharacterLiteralAstNode(7, '3'),
                new CharacterLiteralAstNode(8, '3'),
                new CharacterLiteralAstNode(9, '3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testExceptionCases() {
        testExceptionCase(")", 0);
        testExceptionCase("abc)", 3);
    }
    
    private void testExceptionCase(String pattern, int expectedPosition) {
        RegexParser parser = new RegexParser();
        assertThatThrownBy(() -> parser.parse(pattern))
                .isInstanceOf(RegexParserException.class)
                .extracting(e -> ((RegexParserException) e).position()).as("pattern error position")
                .isEqualTo(expectedPosition);
    }
    
}
