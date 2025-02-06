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
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, leafOnly);
        TreeNode resultNode = transformer.weed(rootNode);
        assertThat(resultNode).isEqualTo(TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty()))));
    }

    @Test
    void testAlternations() {
        TreeWeedingTransformer transformer = new TreeWeedingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(
                        TreeNode.of(charClass("b"), ImmutableList.of(
                                TreeNode.of(charClass("c"), leafOnly))))),
                TreeNode.of(null, ImmutableList.of(
                        TreeNode.of(charClass("m"), ImmutableList.of(
                                TreeNode.of(charClass("n"), leafOnly))),
                        TreeNode.of(charClass("x"), ImmutableList.of(
                                TreeNode.of(charClass("y"), ImmutableList.of(
                                        TreeNode.of(null, ImmutableList.of(
                                                leafNode,
                                                TreeNode.of(charClass("z"), leafOnly)
        ))))))))));
        TreeNode expectedTreeNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(
                        TreeNode.of(charClass("b"), ImmutableList.of(
                                TreeNode.of(charClass("c"), leafOnly))))),
                        TreeNode.of(charClass("m"), ImmutableList.of(
                                TreeNode.of(charClass("n"), leafOnly))),
                        TreeNode.of(charClass("x"), ImmutableList.of(
                                TreeNode.of(charClass("y"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("z"), leafOnly)
        ))))));
        TreeNode resultNode = transformer.weed(rootNode);
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testAnchor() {
        TreeWeedingTransformer transformer = new TreeWeedingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(
                        TreeNode.of(AnchorAstNode.WORD_BOUNDARY, ImmutableList.of(
                                TreeNode.of(null, ImmutableList.of(
                                        TreeNode.of(charClass("="), leafOnly),
                                        TreeNode.of(charClass("x"), leafOnly)
        ))))))));
        TreeNode expectedTreeNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(
                        TreeNode.of(charClass("="), leafOnly)
        ))));
        TreeNode resultNodes = transformer.weed(rootNode);
        assertThat(resultNodes).isEqualTo(expectedTreeNode);
    }

    private CharClass charClass(String chars) {
        return CharClass.of(chars, charComparator);
    }

}
