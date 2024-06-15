package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
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
    void testEmptyGroup() {
        String pattern = "()";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new GroupAstNode(0, new AlternationAstNode(1, ImmutableList.of(
                    new SequenceAstNode(1, ImmutableList.empty())
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleGroup() {
        String pattern = "(lorem)";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new GroupAstNode(0, new AlternationAstNode(1, ImmutableList.of(
                    new SequenceAstNode(1, ImmutableList.of(
                        new CharacterLiteralAstNode(1, 'l'),
                        new CharacterLiteralAstNode(2, 'o'),
                        new CharacterLiteralAstNode(3, 'r'),
                        new CharacterLiteralAstNode(4, 'e'),
                        new CharacterLiteralAstNode(5, 'm')
                    ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testAlternationGroup() {
        String pattern = "(lorem|ipsum)";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new GroupAstNode(0, new AlternationAstNode(1, ImmutableList.of(
                    new SequenceAstNode(1, ImmutableList.of(
                        new CharacterLiteralAstNode(1, 'l'),
                        new CharacterLiteralAstNode(2, 'o'),
                        new CharacterLiteralAstNode(3, 'r'),
                        new CharacterLiteralAstNode(4, 'e'),
                        new CharacterLiteralAstNode(5, 'm')
                    )),
                    new SequenceAstNode(7, ImmutableList.of(
                            new CharacterLiteralAstNode(7, 'i'),
                            new CharacterLiteralAstNode(8, 'p'),
                            new CharacterLiteralAstNode(9, 's'),
                            new CharacterLiteralAstNode(10, 'u'),
                            new CharacterLiteralAstNode(11, 'm')
                        ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testGroupKinds() {
        String pattern = "(lorem)(?:ipsum)(?<dolor>sit)(?'amet'consectetur)(?P<adipiscing>elit)";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new GroupAstNode(0, new AlternationAstNode(1, ImmutableList.of(
                    new SequenceAstNode(1, ImmutableList.of(
                        new CharacterLiteralAstNode(1, 'l'),
                        new CharacterLiteralAstNode(2, 'o'),
                        new CharacterLiteralAstNode(3, 'r'),
                        new CharacterLiteralAstNode(4, 'e'),
                        new CharacterLiteralAstNode(5, 'm')
                    ))
                )), GroupAstNode.Kind.CAPTURING, ""),
                new GroupAstNode(7, new AlternationAstNode(10, ImmutableList.of(
                    new SequenceAstNode(10, ImmutableList.of(
                        new CharacterLiteralAstNode(10, 'i'),
                        new CharacterLiteralAstNode(11, 'p'),
                        new CharacterLiteralAstNode(12, 's'),
                        new CharacterLiteralAstNode(13, 'u'),
                        new CharacterLiteralAstNode(14, 'm')
                    ))
                )), GroupAstNode.Kind.NON_CAPTURING, ""),
                new GroupAstNode(16, new AlternationAstNode(25, ImmutableList.of(
                    new SequenceAstNode(25, ImmutableList.of(
                        new CharacterLiteralAstNode(25, 's'),
                        new CharacterLiteralAstNode(26, 'i'),
                        new CharacterLiteralAstNode(27, 't')
                    ))
                )), GroupAstNode.Kind.NAMED, "dolor"),
                new GroupAstNode(29, new AlternationAstNode(37, ImmutableList.of(
                    new SequenceAstNode(37, ImmutableList.of(
                        new CharacterLiteralAstNode(37, 'c'),
                        new CharacterLiteralAstNode(38, 'o'),
                        new CharacterLiteralAstNode(39, 'n'),
                        new CharacterLiteralAstNode(40, 's'),
                        new CharacterLiteralAstNode(41, 'e'),
                        new CharacterLiteralAstNode(42, 'c'),
                        new CharacterLiteralAstNode(43, 't'),
                        new CharacterLiteralAstNode(44, 'e'),
                        new CharacterLiteralAstNode(45, 't'),
                        new CharacterLiteralAstNode(46, 'u'),
                        new CharacterLiteralAstNode(47, 'r')
                    ))
                )), GroupAstNode.Kind.NAMED, "amet"),
                new GroupAstNode(49, new AlternationAstNode(64, ImmutableList.of(
                    new SequenceAstNode(64, ImmutableList.of(
                        new CharacterLiteralAstNode(64, 'e'),
                        new CharacterLiteralAstNode(65, 'l'),
                        new CharacterLiteralAstNode(66, 'i'),
                        new CharacterLiteralAstNode(67, 't')
                    ))
                )), GroupAstNode.Kind.NAMED, "adipiscing")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testNestedGroups() {
        String pattern = "lorem(ipsum|(?:dolor(?<sit>amet)|consectetur))";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new CharacterLiteralAstNode(0, 'l'),
                new CharacterLiteralAstNode(1, 'o'),
                new CharacterLiteralAstNode(2, 'r'),
                new CharacterLiteralAstNode(3, 'e'),
                new CharacterLiteralAstNode(4, 'm'),
                new GroupAstNode(5, new AlternationAstNode(6, ImmutableList.of(
                    new SequenceAstNode(6, ImmutableList.of(
                        new CharacterLiteralAstNode(6, 'i'),
                        new CharacterLiteralAstNode(7, 'p'),
                        new CharacterLiteralAstNode(8, 's'),
                        new CharacterLiteralAstNode(9, 'u'),
                        new CharacterLiteralAstNode(10, 'm')
                    )),
                    new SequenceAstNode(12, ImmutableList.of(
                        new GroupAstNode(12, new AlternationAstNode(15, ImmutableList.of(
                            new SequenceAstNode(15, ImmutableList.of(
                                new CharacterLiteralAstNode(15, 'd'),
                                new CharacterLiteralAstNode(16, 'o'),
                                new CharacterLiteralAstNode(17, 'l'),
                                new CharacterLiteralAstNode(18, 'o'),
                                new CharacterLiteralAstNode(19, 'r'),
                                new GroupAstNode(20, new AlternationAstNode(27, ImmutableList.of(
                                    new SequenceAstNode(27, ImmutableList.of(
                                        new CharacterLiteralAstNode(27, 'a'),
                                        new CharacterLiteralAstNode(28, 'm'),
                                        new CharacterLiteralAstNode(29, 'e'),
                                        new CharacterLiteralAstNode(30, 't')
                                    ))
                                )), GroupAstNode.Kind.NAMED, "sit")
                            )),
                            new SequenceAstNode(33, ImmutableList.of(
                                new CharacterLiteralAstNode(33, 'c'),
                                new CharacterLiteralAstNode(34, 'o'),
                                new CharacterLiteralAstNode(35, 'n'),
                                new CharacterLiteralAstNode(36, 's'),
                                new CharacterLiteralAstNode(37, 'e'),
                                new CharacterLiteralAstNode(38, 'c'),
                                new CharacterLiteralAstNode(39, 't'),
                                new CharacterLiteralAstNode(40, 'e'),
                                new CharacterLiteralAstNode(41, 't'),
                                new CharacterLiteralAstNode(42, 'u'),
                                new CharacterLiteralAstNode(43, 'r')
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
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new QuantifiedAstNode(0, new CharacterLiteralAstNode(0, 'a'), 0, 1),
                new QuantifiedAstNode(2, new CharacterLiteralAstNode(2, 'b'), 0, QuantifiedAstNode.NO_UPPER_LIMIT),
                new QuantifiedAstNode(4, new CharacterLiteralAstNode(4, 'c'), 1, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testQuantifierPlacedInside() {
        String pattern = "a(b(c)?)";
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new CharacterLiteralAstNode(0, 'a'),
                new GroupAstNode(1, new AlternationAstNode(2, ImmutableList.of(
                    new SequenceAstNode(2, ImmutableList.of(
                        new CharacterLiteralAstNode(2, 'b'),
                        new QuantifiedAstNode(3,
                            new GroupAstNode(3, new AlternationAstNode(4, ImmutableList.of(
                                new SequenceAstNode(4, ImmutableList.of(
                                    new CharacterLiteralAstNode(4, 'c')
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
        AstNode expectedAst = new AlternationAstNode(0, ImmutableList.of(
            new SequenceAstNode(0, ImmutableList.of(
                new QuantifiedAstNode(0, new CharacterLiteralAstNode(0, 'a'), 3, 3),
                new QuantifiedAstNode(4, new CharacterLiteralAstNode(4, 'b'), 1, 4),
                new QuantifiedAstNode(10, new CharacterLiteralAstNode(10, 'c'), 2, QuantifiedAstNode.NO_UPPER_LIMIT)
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
    }
    
    private void testExceptionCase(String pattern, int expectedPosition) {
        RegexParser parser = new RegexParser();
        assertThatThrownBy(() -> parser.parse(pattern))
                .isInstanceOf(RegexParserException.class)
                .extracting(e -> ((RegexParserException) e).position()).as("pattern error position")
                .isEqualTo(expectedPosition);
    }
    
}
