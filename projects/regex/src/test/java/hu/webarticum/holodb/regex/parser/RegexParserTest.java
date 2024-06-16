package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
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

    @Test
    void testEmptyGroup() {
        String pattern = "()";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.empty())
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleGroup() {
        String pattern = "(lorem)";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('l'),
                        new CharacterLiteralAstNode('o'),
                        new CharacterLiteralAstNode('r'),
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('m')
                    ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testAlternationGroup() {
        String pattern = "(lorem|ipsum)";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('l'),
                        new CharacterLiteralAstNode('o'),
                        new CharacterLiteralAstNode('r'),
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('m')
                    )),
                    new SequenceAstNode(ImmutableList.of(
                            new CharacterLiteralAstNode('i'),
                            new CharacterLiteralAstNode('p'),
                            new CharacterLiteralAstNode('s'),
                            new CharacterLiteralAstNode('u'),
                            new CharacterLiteralAstNode('m')
                        ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testGroupKinds() {
        String pattern = "(lorem)(?:ipsum)(?<dolor>sit)(?'amet'consectetur)(?P<adipiscing>elit)";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('l'),
                        new CharacterLiteralAstNode('o'),
                        new CharacterLiteralAstNode('r'),
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('m')
                    ))
                )), GroupAstNode.Kind.CAPTURING, ""),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('i'),
                        new CharacterLiteralAstNode('p'),
                        new CharacterLiteralAstNode('s'),
                        new CharacterLiteralAstNode('u'),
                        new CharacterLiteralAstNode('m')
                    ))
                )), GroupAstNode.Kind.NON_CAPTURING, ""),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('s'),
                        new CharacterLiteralAstNode('i'),
                        new CharacterLiteralAstNode('t')
                    ))
                )), GroupAstNode.Kind.NAMED, "dolor"),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('c'),
                        new CharacterLiteralAstNode('o'),
                        new CharacterLiteralAstNode('n'),
                        new CharacterLiteralAstNode('s'),
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('c'),
                        new CharacterLiteralAstNode('t'),
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('t'),
                        new CharacterLiteralAstNode('u'),
                        new CharacterLiteralAstNode('r')
                    ))
                )), GroupAstNode.Kind.NAMED, "amet"),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('e'),
                        new CharacterLiteralAstNode('l'),
                        new CharacterLiteralAstNode('i'),
                        new CharacterLiteralAstNode('t')
                    ))
                )), GroupAstNode.Kind.NAMED, "adipiscing")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testNestedGroups() {
        String pattern = "lorem(ipsum|(?:dolor(?<sit>amet)|consectetur))";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('l'),
                new CharacterLiteralAstNode('o'),
                new CharacterLiteralAstNode('r'),
                new CharacterLiteralAstNode('e'),
                new CharacterLiteralAstNode('m'),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('i'),
                        new CharacterLiteralAstNode('p'),
                        new CharacterLiteralAstNode('s'),
                        new CharacterLiteralAstNode('u'),
                        new CharacterLiteralAstNode('m')
                    )),
                    new SequenceAstNode(ImmutableList.of(
                        new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                            new SequenceAstNode(ImmutableList.of(
                                new CharacterLiteralAstNode('d'),
                                new CharacterLiteralAstNode('o'),
                                new CharacterLiteralAstNode('l'),
                                new CharacterLiteralAstNode('o'),
                                new CharacterLiteralAstNode('r'),
                                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                                    new SequenceAstNode(ImmutableList.of(
                                        new CharacterLiteralAstNode('a'),
                                        new CharacterLiteralAstNode('m'),
                                        new CharacterLiteralAstNode('e'),
                                        new CharacterLiteralAstNode('t')
                                    ))
                                )), GroupAstNode.Kind.NAMED, "sit")
                            )),
                            new SequenceAstNode(ImmutableList.of(
                                new CharacterLiteralAstNode('c'),
                                new CharacterLiteralAstNode('o'),
                                new CharacterLiteralAstNode('n'),
                                new CharacterLiteralAstNode('s'),
                                new CharacterLiteralAstNode('e'),
                                new CharacterLiteralAstNode('c'),
                                new CharacterLiteralAstNode('t'),
                                new CharacterLiteralAstNode('e'),
                                new CharacterLiteralAstNode('t'),
                                new CharacterLiteralAstNode('u'),
                                new CharacterLiteralAstNode('r')
                            ))
                        )), GroupAstNode.Kind.NON_CAPTURING, "")
                    ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testSimpleQuantifiers() {
        String pattern = "a?b*c+";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new QuantifiedAstNode(new CharacterLiteralAstNode('a'), 0, 1),
                new QuantifiedAstNode(new CharacterLiteralAstNode('b'), 0, QuantifiedAstNode.NO_UPPER_LIMIT),
                new QuantifiedAstNode(new CharacterLiteralAstNode('c'), 1, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testQuantifierPlacedInside() {
        String pattern = "a(b(c)?)";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode(ImmutableList.of(
                new CharacterLiteralAstNode('a'),
                new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                    new SequenceAstNode(ImmutableList.of(
                        new CharacterLiteralAstNode('b'),
                        new QuantifiedAstNode(
                            new GroupAstNode(new AlternationAstNode(ImmutableList.of(
                                new SequenceAstNode(ImmutableList.of(
                                    new CharacterLiteralAstNode('c')
                                ))
                            )), GroupAstNode.Kind.CAPTURING, ""),
                        0, 1)
                    ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testExplicitQuantifiers() {
        String pattern = "a{3}b{1,4}c{2,}";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode( ImmutableList.of(
                new QuantifiedAstNode(new CharacterLiteralAstNode('a'), 3, 3),
                new QuantifiedAstNode(new CharacterLiteralAstNode('b'), 1, 4),
                new QuantifiedAstNode(new CharacterLiteralAstNode('c'), 2, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testSimpleEscapeSequences() {
        String pattern = "\\A\\\\?\\\\\\?\\?\\b\\v";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode( ImmutableList.of(
                new AnchorAstNode(AnchorAstNode.Kind.BEGIN_OF_INPUT),
                new QuantifiedAstNode(new CharacterLiteralAstNode('\\'), 0, 1),
                new CharacterLiteralAstNode('\\'),
                new CharacterLiteralAstNode('?'),
                new CharacterLiteralAstNode('?'),
                new AnchorAstNode(AnchorAstNode.Kind.WORD_BOUNDARY),
                new BuiltinCharacterClassAstNode(BuiltinCharacterClassAstNode.Kind.VERTICAL_WHITESPACE)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testOctalEscapeSequences() {
        String pattern = "\\075e";
        AstNode expectedAst = new AlternationAstNode(ImmutableList.of(
            new SequenceAstNode( ImmutableList.of(
                new CharacterLiteralAstNode('='),
                new CharacterLiteralAstNode('e')
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testExceptionCases() {
        testExceptionCase(")", 0);
        testExceptionCase("abc)", 3);
        testExceptionCase("(", 1);
        testExceptionCase("lorem(ipsum", 11);
        testExceptionCase("lorem)ipsum", 5);
        testExceptionCase("(?lorem)", 2);
        testExceptionCase("(?<12>)", 3);
        testExceptionCase("(?<!!!>)", 3);
        testExceptionCase("(?'%%%')", 3);
        testExceptionCase("(?P<&&&>)", 4);
        testExceptionCase("lorem((ipsum)|(?:dolor)sit", 26);
        testExceptionCase("a++", 1);
        testExceptionCase("a*?", 1);
        testExceptionCase("a***", 2);
        testExceptionCase("a{}", 2);
        testExceptionCase("a{x}", 2);
        testExceptionCase("a{3}{4}", 4);
        testExceptionCase("\\", 1);
        testExceptionCase("abc\\", 4);
        testExceptionCase("lorem\\ipsum", 5);
    }
    
    private void testExceptionCase(String pattern, int expectedPosition) {
        RegexParser parser = new RegexParser();
        assertThatThrownBy(() -> parser.parse(pattern))
                .isInstanceOf(RegexParserException.class)
                .extracting(e -> ((RegexParserException) e).position()).as("pattern error position")
                .isEqualTo(expectedPosition);
    }
    
}
