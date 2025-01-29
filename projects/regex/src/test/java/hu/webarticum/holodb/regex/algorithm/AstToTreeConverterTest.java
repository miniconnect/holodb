package hu.webarticum.holodb.regex.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AlternationAstNode;
import hu.webarticum.holodb.regex.ast.CharacterConstantAstNode;
import hu.webarticum.holodb.regex.ast.GroupAstNode;
import hu.webarticum.holodb.regex.ast.QuantifiedAstNode;
import hu.webarticum.holodb.regex.ast.SequenceAstNode;
import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class AstToTreeConverterTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testEmpty() {
        AstToTreeConverter converter = new AstToTreeConverter(charComparator);
        AlternationAstNode alternationNode =
                AlternationAstNode.of(ImmutableList.of(SequenceAstNode.of(ImmutableList.empty())));
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode expectedTreeNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(leafNode));
        assertThat(converter.convert(alternationNode)).isEqualTo(expectedTreeNode);
    }

    @Test
    void testAlternationOfSequences() {
        AstToTreeConverter converter = new AstToTreeConverter(charComparator);
        AlternationAstNode alternationNode = AlternationAstNode.of(ImmutableList.of(
                SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('a'),
                        CharacterConstantAstNode.of('b'),
                        CharacterConstantAstNode.of('c'))),
                SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('m'),
                        CharacterConstantAstNode.of('n'))),
                SequenceAstNode.of(ImmutableList.of(
                        CharacterConstantAstNode.of('x'),
                        CharacterConstantAstNode.of('y'),
                        CharacterConstantAstNode.of('z')))
        ));
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode expectedTreeNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("b", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("c", charComparator), ImmutableList.of(leafNode)))))),
                new TreeNode(CharClass.of("m", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("n", charComparator), ImmutableList.of(leafNode)))),
                new TreeNode(CharClass.of("x", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("y", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("z", charComparator), ImmutableList.of(leafNode))))))));
        assertThat(converter.convert(alternationNode)).isEqualTo(expectedTreeNode);
    }

    @Test
    void testQuantifier() {
        AstToTreeConverter converter = new AstToTreeConverter(charComparator);
        AlternationAstNode alternationNode = AlternationAstNode.of(ImmutableList.of(
                SequenceAstNode.of(ImmutableList.of(
                        QuantifiedAstNode.of(
                            GroupAstNode.of(AlternationAstNode.of(ImmutableList.of(
                                    SequenceAstNode.of(ImmutableList.of(
                                            CharacterConstantAstNode.of('s'),
                                            CharacterConstantAstNode.of('t')))
                            )), GroupAstNode.Kind.CAPTURING, ""),
                            1, 2
                        )
                ))
        ));
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode expectedTreeNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(null, ImmutableList.of(
                        new TreeNode(CharClass.of("s", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("t", charComparator), ImmutableList.of(
                                        leafNode,
                                        new TreeNode(null, ImmutableList.of(
                                            new TreeNode(CharClass.of("s", charComparator), ImmutableList.of(
                                                    new TreeNode(CharClass.of("t", charComparator),
                                                            ImmutableList.of(leafNode)
                                                    )
                                            ))
                                        ))
                                ))
                        )
                )))
        ));
        assertThat(converter.convert(alternationNode)).isEqualTo(expectedTreeNode);
    }
    
}
