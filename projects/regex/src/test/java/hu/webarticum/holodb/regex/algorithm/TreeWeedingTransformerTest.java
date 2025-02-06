package hu.webarticum.holodb.regex.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.ast.AnchorAstNode;
import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class TreeWeedingTransformerTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testEmpty() {
        TreeWeedingTransformer transformer = new TreeWeedingTransformer();
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode rootNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(leafNode));
        TreeNode resultNode = transformer.weed(rootNode);
        assertThat(resultNode).isEqualTo(new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty()))));
    }

    @Test
    void testAlternations() {
        TreeWeedingTransformer transformer = new TreeWeedingTransformer();
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode rootNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("b", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("c", charComparator), ImmutableList.of(leafNode)))))),
                new TreeNode(null, ImmutableList.of(
                        new TreeNode(CharClass.of("m", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("n", charComparator), ImmutableList.of(leafNode)))),
                        new TreeNode(CharClass.of("x", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("y", charComparator), ImmutableList.of(
                                        new TreeNode(null, ImmutableList.of(
                                                leafNode,
                                                new TreeNode(
                                                        CharClass.of("z", charComparator), ImmutableList.of(leafNode))
        ))))))))));
        TreeNode expectedTreeNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("b", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("c", charComparator), ImmutableList.of(leafNode)))))),
                        new TreeNode(CharClass.of("m", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("n", charComparator), ImmutableList.of(leafNode)))),
                        new TreeNode(CharClass.of("x", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("y", charComparator), ImmutableList.of(
                                        leafNode,
                                        new TreeNode(CharClass.of("z", charComparator), ImmutableList.of(leafNode))
        ))))));
        TreeNode resultNode = transformer.weed(rootNode);
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testAnchor() {
        TreeWeedingTransformer transformer = new TreeWeedingTransformer();
        TreeNode leafNode = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode rootNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(AnchorAstNode.WORD_BOUNDARY, ImmutableList.of(
                                new TreeNode(null, ImmutableList.of(
                                        new TreeNode(CharClass.of("=", charComparator), ImmutableList.of(leafNode)),
                                        new TreeNode(CharClass.of("x", charComparator), ImmutableList.of(leafNode))
        ))))))));
        TreeNode expectedTreeNode = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("=", charComparator), ImmutableList.of(leafNode))
        ))));
        TreeNode resultNodes = transformer.weed(rootNode);
        assertThat(resultNodes).isEqualTo(expectedTreeNode);
    }

}
