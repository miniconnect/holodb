package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.BackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterLiteralAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.LinebreakAstNode;
import hu.webarticum.holodb.regex.ast.NamedBackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class RegexParserTest {

    @Test
    void testEmpty() {
        String pattern = "";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.empty())
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleCharacters() {
        String pattern = "abc123";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('a'),
                CharacterLiteralAstNode.of('b'),
                CharacterLiteralAstNode.of('c'),
                CharacterLiteralAstNode.of('1'),
                CharacterLiteralAstNode.of('2'),
                CharacterLiteralAstNode.of('3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleAlternation() {
        String pattern = "a1|b2|c333";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('a'),
                CharacterLiteralAstNode.of('1')
            )),
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('b'),
                CharacterLiteralAstNode.of('2')
            )),
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('c'),
                CharacterLiteralAstNode.of('3'),
                CharacterLiteralAstNode.of('3'),
                CharacterLiteralAstNode.of('3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testEmptyGroup() {
        String pattern = "()";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.empty())
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleGroup() {
        String pattern = "(lorem)";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('l'),
                        CharacterLiteralAstNode.of('o'),
                        CharacterLiteralAstNode.of('r'),
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('m')
                    ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testAlternationGroup() {
        String pattern = "(lorem|ipsum)";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('l'),
                        CharacterLiteralAstNode.of('o'),
                        CharacterLiteralAstNode.of('r'),
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('m')
                    )),
                    SequenceAstNode.of(ImmutableList.of(
                            CharacterLiteralAstNode.of('i'),
                            CharacterLiteralAstNode.of('p'),
                            CharacterLiteralAstNode.of('s'),
                            CharacterLiteralAstNode.of('u'),
                            CharacterLiteralAstNode.of('m')
                        ))
                )), GroupAstNode.Kind.CAPTURING, "")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testGroupKinds() {
        String pattern = "(lorem)(?:ipsum)(?<dolor>sit)(?'amet'consectetur)(?P<adipiscing>elit)";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('l'),
                        CharacterLiteralAstNode.of('o'),
                        CharacterLiteralAstNode.of('r'),
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('m')
                    ))
                )), GroupAstNode.Kind.CAPTURING, ""),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('i'),
                        CharacterLiteralAstNode.of('p'),
                        CharacterLiteralAstNode.of('s'),
                        CharacterLiteralAstNode.of('u'),
                        CharacterLiteralAstNode.of('m')
                    ))
                )), GroupAstNode.Kind.NON_CAPTURING, ""),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('s'),
                        CharacterLiteralAstNode.of('i'),
                        CharacterLiteralAstNode.of('t')
                    ))
                )), GroupAstNode.Kind.NAMED, "dolor"),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('c'),
                        CharacterLiteralAstNode.of('o'),
                        CharacterLiteralAstNode.of('n'),
                        CharacterLiteralAstNode.of('s'),
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('c'),
                        CharacterLiteralAstNode.of('t'),
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('t'),
                        CharacterLiteralAstNode.of('u'),
                        CharacterLiteralAstNode.of('r')
                    ))
                )), GroupAstNode.Kind.NAMED, "amet"),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('e'),
                        CharacterLiteralAstNode.of('l'),
                        CharacterLiteralAstNode.of('i'),
                        CharacterLiteralAstNode.of('t')
                    ))
                )), GroupAstNode.Kind.NAMED, "adipiscing")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testNestedGroups() {
        String pattern = "lorem(ipsum|(?:dolor(?<sit>amet)|consectetur))";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('l'),
                CharacterLiteralAstNode.of('o'),
                CharacterLiteralAstNode.of('r'),
                CharacterLiteralAstNode.of('e'),
                CharacterLiteralAstNode.of('m'),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('i'),
                        CharacterLiteralAstNode.of('p'),
                        CharacterLiteralAstNode.of('s'),
                        CharacterLiteralAstNode.of('u'),
                        CharacterLiteralAstNode.of('m')
                    )),
                    SequenceAstNode.of(ImmutableList.of(
                        GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                            SequenceAstNode.of(ImmutableList.of(
                                CharacterLiteralAstNode.of('d'),
                                CharacterLiteralAstNode.of('o'),
                                CharacterLiteralAstNode.of('l'),
                                CharacterLiteralAstNode.of('o'),
                                CharacterLiteralAstNode.of('r'),
                                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                                    SequenceAstNode.of(ImmutableList.of(
                                        CharacterLiteralAstNode.of('a'),
                                        CharacterLiteralAstNode.of('m'),
                                        CharacterLiteralAstNode.of('e'),
                                        CharacterLiteralAstNode.of('t')
                                    ))
                                )), GroupAstNode.Kind.NAMED, "sit")
                            )),
                            SequenceAstNode.of(ImmutableList.of(
                                CharacterLiteralAstNode.of('c'),
                                CharacterLiteralAstNode.of('o'),
                                CharacterLiteralAstNode.of('n'),
                                CharacterLiteralAstNode.of('s'),
                                CharacterLiteralAstNode.of('e'),
                                CharacterLiteralAstNode.of('c'),
                                CharacterLiteralAstNode.of('t'),
                                CharacterLiteralAstNode.of('e'),
                                CharacterLiteralAstNode.of('t'),
                                CharacterLiteralAstNode.of('u'),
                                CharacterLiteralAstNode.of('r')
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
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('a'), 0, 1),
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('b'), 0, QuantifiedAstNode.NO_UPPER_LIMIT),
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('c'), 1, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testQuantifierPlacedInside() {
        String pattern = "a(b(c)?)";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('a'),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('b'),
                        QuantifiedAstNode.of(
                            GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                                SequenceAstNode.of(ImmutableList.of(
                                    CharacterLiteralAstNode.of('c')
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
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('a'), 3, 3),
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('b'), 1, 4),
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('c'), 2, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testSimpleEscapeSequences() {
        String pattern = "\\A\\\\?\\\\\\?\\?\\b\\v\\R";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                AnchorAstNode.BEGIN_OF_INPUT,
                QuantifiedAstNode.of(CharacterLiteralAstNode.of('\\'), 0, 1),
                CharacterLiteralAstNode.of('\\'),
                CharacterLiteralAstNode.of('?'),
                CharacterLiteralAstNode.of('?'),
                AnchorAstNode.WORD_BOUNDARY,
                BuiltinCharacterClassAstNode.VERTICAL_WHITESPACE,
                LinebreakAstNode.instance()
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testOctalEscapeSequences() {
        String pattern = "\\075e";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterLiteralAstNode.of('='),
                CharacterLiteralAstNode.of('e')
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testBackreference() {
        String pattern = "(a)\\1";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('a')
                    ))
                )), GroupAstNode.Kind.CAPTURING, ""),
                BackreferenceAstNode.of(1)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testNamedBackreference() {
        String pattern = "(?<x>a)\\k<x>";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterLiteralAstNode.of('a')
                    ))
                )), GroupAstNode.Kind.NAMED, "x"),
                NamedBackreferenceAstNode.of("x")
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
