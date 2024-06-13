package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;

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
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.empty())
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleCharacters() {
        String pattern = "abc123";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('a'),
                new CharacterLiteralAstNode('b'),
                new CharacterLiteralAstNode('c'),
                new CharacterLiteralAstNode('1'),
                new CharacterLiteralAstNode('2'),
                new CharacterLiteralAstNode('3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleAlternation() {
        String pattern = "a1|b2|c333";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('a'),
                new CharacterLiteralAstNode('1')
            )),
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('b'),
                new CharacterLiteralAstNode('2')
            )),
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('c'),
                new CharacterLiteralAstNode('3'),
                new CharacterLiteralAstNode('3'),
                new CharacterLiteralAstNode('3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
}
