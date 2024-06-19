package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.ast.AstNode;
import hu.webarticum.holodb.regex.ast.BackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.BuiltinCharacterClassAstNode;
import hu.webarticum.holodb.regex.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.LinebreakAstNode;
import hu.webarticum.holodb.regex.ast.NamedBackreferenceAstNode;
import hu.webarticum.holodb.regex.ast.PropertyCharacterClassAstNode;
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
                CharacterConstantAstNode.of('a'),
                CharacterConstantAstNode.of('b'),
                CharacterConstantAstNode.of('c'),
                CharacterConstantAstNode.of('1'),
                CharacterConstantAstNode.of('2'),
                CharacterConstantAstNode.of('3')
            ))
        ));
        
        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testSimpleAlternation() {
        String pattern = "a1|b2|c333";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('a'),
                CharacterConstantAstNode.of('1')
            )),
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('b'),
                CharacterConstantAstNode.of('2')
            )),
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('c'),
                CharacterConstantAstNode.of('3'),
                CharacterConstantAstNode.of('3'),
                CharacterConstantAstNode.of('3')
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
                        CharacterConstantAstNode.of('l'),
                        CharacterConstantAstNode.of('o'),
                        CharacterConstantAstNode.of('r'),
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('m')
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
                        CharacterConstantAstNode.of('l'),
                        CharacterConstantAstNode.of('o'),
                        CharacterConstantAstNode.of('r'),
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('m')
                    )),
                    SequenceAstNode.of(ImmutableList.of(
                            CharacterConstantAstNode.of('i'),
                            CharacterConstantAstNode.of('p'),
                            CharacterConstantAstNode.of('s'),
                            CharacterConstantAstNode.of('u'),
                            CharacterConstantAstNode.of('m')
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
                        CharacterConstantAstNode.of('l'),
                        CharacterConstantAstNode.of('o'),
                        CharacterConstantAstNode.of('r'),
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('m')
                    ))
                )), GroupAstNode.Kind.CAPTURING, ""),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('i'),
                        CharacterConstantAstNode.of('p'),
                        CharacterConstantAstNode.of('s'),
                        CharacterConstantAstNode.of('u'),
                        CharacterConstantAstNode.of('m')
                    ))
                )), GroupAstNode.Kind.NON_CAPTURING, ""),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('s'),
                        CharacterConstantAstNode.of('i'),
                        CharacterConstantAstNode.of('t')
                    ))
                )), GroupAstNode.Kind.NAMED, "dolor"),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('c'),
                        CharacterConstantAstNode.of('o'),
                        CharacterConstantAstNode.of('n'),
                        CharacterConstantAstNode.of('s'),
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('c'),
                        CharacterConstantAstNode.of('t'),
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('t'),
                        CharacterConstantAstNode.of('u'),
                        CharacterConstantAstNode.of('r')
                    ))
                )), GroupAstNode.Kind.NAMED, "amet"),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('e'),
                        CharacterConstantAstNode.of('l'),
                        CharacterConstantAstNode.of('i'),
                        CharacterConstantAstNode.of('t')
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
                CharacterConstantAstNode.of('l'),
                CharacterConstantAstNode.of('o'),
                CharacterConstantAstNode.of('r'),
                CharacterConstantAstNode.of('e'),
                CharacterConstantAstNode.of('m'),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('i'),
                        CharacterConstantAstNode.of('p'),
                        CharacterConstantAstNode.of('s'),
                        CharacterConstantAstNode.of('u'),
                        CharacterConstantAstNode.of('m')
                    )),
                    SequenceAstNode.of(ImmutableList.of(
                        GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                            SequenceAstNode.of(ImmutableList.of(
                                CharacterConstantAstNode.of('d'),
                                CharacterConstantAstNode.of('o'),
                                CharacterConstantAstNode.of('l'),
                                CharacterConstantAstNode.of('o'),
                                CharacterConstantAstNode.of('r'),
                                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                                    SequenceAstNode.of(ImmutableList.of(
                                        CharacterConstantAstNode.of('a'),
                                        CharacterConstantAstNode.of('m'),
                                        CharacterConstantAstNode.of('e'),
                                        CharacterConstantAstNode.of('t')
                                    ))
                                )), GroupAstNode.Kind.NAMED, "sit")
                            )),
                            SequenceAstNode.of(ImmutableList.of(
                                CharacterConstantAstNode.of('c'),
                                CharacterConstantAstNode.of('o'),
                                CharacterConstantAstNode.of('n'),
                                CharacterConstantAstNode.of('s'),
                                CharacterConstantAstNode.of('e'),
                                CharacterConstantAstNode.of('c'),
                                CharacterConstantAstNode.of('t'),
                                CharacterConstantAstNode.of('e'),
                                CharacterConstantAstNode.of('t'),
                                CharacterConstantAstNode.of('u'),
                                CharacterConstantAstNode.of('r')
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
                QuantifiedAstNode.of(CharacterConstantAstNode.of('a'), 0, 1),
                QuantifiedAstNode.of(CharacterConstantAstNode.of('b'), 0, QuantifiedAstNode.NO_UPPER_LIMIT),
                QuantifiedAstNode.of(CharacterConstantAstNode.of('c'), 1, QuantifiedAstNode.NO_UPPER_LIMIT)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testQuantifierPlacedInside() {
        String pattern = "a(b(c)?)";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('a'),
                GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                    SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('b'),
                        QuantifiedAstNode.of(
                            GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                                SequenceAstNode.of(ImmutableList.of(
                                    CharacterConstantAstNode.of('c')
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
                QuantifiedAstNode.of(CharacterConstantAstNode.of('a'), 3, 3),
                QuantifiedAstNode.of(CharacterConstantAstNode.of('b'), 1, 4),
                QuantifiedAstNode.of(CharacterConstantAstNode.of('c'), 2, QuantifiedAstNode.NO_UPPER_LIMIT)
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
                QuantifiedAstNode.of(CharacterConstantAstNode.of('\\'), 0, 1),
                CharacterConstantAstNode.of('\\'),
                CharacterConstantAstNode.of('?'),
                CharacterConstantAstNode.of('?'),
                AnchorAstNode.WORD_BOUNDARY,
                BuiltinCharacterClassAstNode.VERTICAL_WHITESPACE,
                LinebreakAstNode.instance()
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testOctalEscapeSequence() {
        String pattern = "\\075e";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('='),
                CharacterConstantAstNode.of('e')
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testHexadecimalEscapeSequences() {
        String pattern = "\\x4C\\\\u\\u0050";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('L'),
                CharacterConstantAstNode.of('\\'),
                CharacterConstantAstNode.of('u'),
                CharacterConstantAstNode.of('P')
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testBracedHexadecimalEscapeSequences() {
        String pattern = "a\\x{10A}b\\u{10B}c";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('a'),
                CharacterConstantAstNode.of('Ċ'),
                CharacterConstantAstNode.of('b'),
                CharacterConstantAstNode.of('ċ'),
                CharacterConstantAstNode.of('c')
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }

    @Test
    void testControlCharacterEscapeSequences() {
        String pattern = "\\c@\\cj\\cAB\\c]\\c??";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                CharacterConstantAstNode.of('\u0000'),
                CharacterConstantAstNode.of('\n'),
                CharacterConstantAstNode.of('\u0001'),
                CharacterConstantAstNode.of('B'),
                CharacterConstantAstNode.of('\u001D'),
                QuantifiedAstNode.of(CharacterConstantAstNode.of('\u007F'), 0, 1)
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testProperyCharacterClasses() {
        String pattern = "\\p{Digit}\\p{Alnum}x\\P{IsControl}y";
        AstNode expectedAst = AlternationAstNode.of(ImmutableList.of(
            SequenceAstNode.of(ImmutableList.of(
                PropertyCharacterClassAstNode.of(PropertyCharacterClassAstNode.Property.DIGIT, true),
                PropertyCharacterClassAstNode.of(PropertyCharacterClassAstNode.Property.ALNUM, true),
                CharacterConstantAstNode.of('x'),
                PropertyCharacterClassAstNode.of(PropertyCharacterClassAstNode.Property.CONTROL, false),
                CharacterConstantAstNode.of('y')
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
                        CharacterConstantAstNode.of('a')
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
                        CharacterConstantAstNode.of('a')
                    ))
                )), GroupAstNode.Kind.NAMED, "x"),
                NamedBackreferenceAstNode.of("x")
            ))
        ));

        assertThat(new RegexParser().parse(pattern)).isEqualTo(expectedAst);
    }
    
    @Test
    void testExceptionCases() { // NOSONAR this is easy to read
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
        testExceptionCase("\\xz", 3);
        testExceptionCase("\\x4", 3);
        testExceptionCase("\\x4z", 2);
        testExceptionCase("\\x3u", 2);
        testExceptionCase("\\u111", 5);
        testExceptionCase("\\u111u", 2);
        testExceptionCase("\\x{A3", 5);
        testExceptionCase("\\u{111X}", 6);
        testExceptionCase("a\\kk", 3);
        testExceptionCase("\\c", 2);
        testExceptionCase("\\c!", 2);
        testExceptionCase("\\p{Alnum", 8);
        testExceptionCase("\\P{Alnum!}", 8);
        testExceptionCase("\\p{Lorem}", 3);
        testExceptionCase("\\\\p{Digit}", 4);
    }
    
    private void testExceptionCase(String pattern, int expectedPosition) {
        RegexParser parser = new RegexParser();
        assertThatThrownBy(() -> parser.parse(pattern))
                .isInstanceOf(RegexParserException.class)
                .extracting(e -> ((RegexParserException) e).position()).as("pattern error position")
                .isEqualTo(expectedPosition);
    }
    
}
