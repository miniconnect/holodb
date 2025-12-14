package hu.webarticum.holodb.regex.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class TreeSortingTransformerTest {

    private final CharComparator charComparator = Character::compare;

    @Test
    void testEmpty() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, leafOnly);
        TreeNode resultNode = transformer.sort(rootNode);
        assertThat(resultNode).isEqualTo(TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty()))));
    }

    @Test
    void testUnchanging() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
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
        TreeNode resultNode = transformer.sort(rootNode);
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
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testSimpleSort() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("x"), leafOnly),
                TreeNode.of(charClass("a"), leafOnly),
                TreeNode.of(charClass("z"), leafOnly),
                TreeNode.of(charClass("c"), leafOnly)
        ));
        TreeNode resultNode = transformer.sort(rootNode);
        TreeNode expectedTreeNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), leafOnly),
                TreeNode.of(charClass("c"), leafOnly),
                TreeNode.of(charClass("x"), leafOnly),
                TreeNode.of(charClass("z"), leafOnly)
        ));
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testSimpleMerge() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("ad"), ImmutableList.of(TreeNode.of(charClass("1"), leafOnly))),
                TreeNode.of(charClass("bc"), ImmutableList.of(TreeNode.of(charClass("2"), leafOnly))),
                TreeNode.of(charClass("fg"), ImmutableList.of(TreeNode.of(charClass("3"), leafOnly))),
                TreeNode.of(charClass("df"), ImmutableList.of(TreeNode.of(charClass("4"), leafOnly)))
        ));
        TreeNode resultNode = transformer.sort(rootNode);
        TreeNode expectedTreeNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(TreeNode.of(charClass("1"), leafOnly))),
                TreeNode.of(charClass("bc"), ImmutableList.of(TreeNode.of(charClass("2"), leafOnly))),
                TreeNode.of(charClass("d"), ImmutableList.of(TreeNode.of(charClass("1"), leafOnly), TreeNode.of(charClass("4"), leafOnly))),
                TreeNode.of(charClass("f"), ImmutableList.of(TreeNode.of(charClass("3"), leafOnly), TreeNode.of(charClass("4"), leafOnly))),
                TreeNode.of(charClass("g"), ImmutableList.of(TreeNode.of(charClass("3"), leafOnly)))
        ));
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testComplex() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("ac"), ImmutableList.of(
                        TreeNode.of(charClass("xy"), ImmutableList.of(
                                TreeNode.of(charClass("st"), leafOnly))),
                        leafNode)),
                TreeNode.of(charClass("ae"), ImmutableList.of(
                        TreeNode.of(charClass("x"), ImmutableList.of(
                                TreeNode.of(charClass("sv"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))))),
                        TreeNode.of(charClass("xz"), ImmutableList.of(
                                TreeNode.of(charClass("stv"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))))))),
                TreeNode.of(charClass("b"), ImmutableList.of(
                        TreeNode.of(charClass("abc"), ImmutableList.of(
                                leafNode,
                                TreeNode.of(charClass("x"), leafOnly))),
                        TreeNode.of(charClass("cde"), leafOnly),
                        TreeNode.of(charClass("bcd"), leafOnly)))
        ));
        TreeNode resultNode = transformer.sort(rootNode);
        TreeNode expectedTreeNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("a"), ImmutableList.of(
                        leafNode,
                        TreeNode.of(charClass("x"), ImmutableList.of(
                                TreeNode.of(charClass("s"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))),
                                TreeNode.of(charClass("t"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))),
                                TreeNode.of(charClass("v"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))))),
                        TreeNode.of(charClass("y"), ImmutableList.of(
                                TreeNode.of(charClass("st"), leafOnly))),
                        TreeNode.of(charClass("z"), ImmutableList.of(
                                TreeNode.of(charClass("stv"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))))))),
                TreeNode.of(charClass("b"), ImmutableList.of(
                        TreeNode.of(charClass("a"), ImmutableList.of(
                                leafNode,
                                TreeNode.of(charClass("x"), leafOnly))),
                        TreeNode.of(charClass("b"), ImmutableList.of(
                                leafNode,
                                TreeNode.of(charClass("x"), leafOnly))),
                        TreeNode.of(charClass("c"), ImmutableList.of(
                                leafNode,
                                TreeNode.of(charClass("x"), leafOnly))),
                        TreeNode.of(charClass("d"), leafOnly),
                        TreeNode.of(charClass("e"), leafOnly))),
                TreeNode.of(charClass("c"), ImmutableList.of(
                        leafNode,
                        TreeNode.of(charClass("xy"), ImmutableList.of(TreeNode.of(charClass("st"), leafOnly))))),
                TreeNode.of(charClass("e"), ImmutableList.of(
                        TreeNode.of(charClass("x"), ImmutableList.of(
                                TreeNode.of(charClass("s"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))),
                                TreeNode.of(charClass("t"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))),
                                TreeNode.of(charClass("v"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly))))),
                        TreeNode.of(charClass("z"), ImmutableList.of(
                                TreeNode.of(charClass("stv"), ImmutableList.of(
                                        leafNode,
                                        TreeNode.of(charClass("a"), leafOnly)))))))
        ));
        assertThat(resultNode).isEqualTo(expectedTreeNode);
    }

    @Test
    void testUnchangingLazyness() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
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
        TreeNode resultNode = transformer.sort(rootNode);
        assertThat(resultNode).isSameAs(rootNode);
    }

    @Test
    void testSubLaziness() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode subNode = TreeNode.of(charClass("abc"), ImmutableList.of(
                TreeNode.of(charClass("g"), ImmutableList.of(
                        leafNode,
                        TreeNode.of(charClass("x"), leafOnly),
                        TreeNode.of(charClass("y"), leafOnly),
                        TreeNode.of(charClass("z"), leafOnly)))
        ));
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                subNode,
                leafNode));
        TreeNode resultNode = transformer.sort(rootNode);
        assertThat(resultNode).isNotSameAs(rootNode).isNotEqualTo(rootNode);
        TreeNode resultSecondChild = resultNode.children().get(1);
        assertThat(resultSecondChild.value()).isEqualTo(charClass("abc"));
        assertThat(resultSecondChild).isSameAs(subNode);
    }

    @Test
    void testCaching() {
        TreeSortingTransformer transformer = new TreeSortingTransformer();
        TreeNode leafNode = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        ImmutableList<TreeNode> leafOnly = ImmutableList.of(leafNode);
        TreeNode subNode = TreeNode.of(charClass("abc"), ImmutableList.of(
                TreeNode.of(charClass("x"), leafOnly),
                leafNode
        ));
        TreeNode rootNode = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(charClass("z"), ImmutableList.of(subNode)),
                subNode
        ));
        TreeNode resultNode = transformer.sort(rootNode);
        assertThat(resultNode).isNotSameAs(rootNode).isNotEqualTo(rootNode);
        TreeNode resultFirstSubNode = resultNode.children().get(0);
        assertThat(resultFirstSubNode.value()).isEqualTo(charClass("abc"));
        assertThat(resultFirstSubNode).isNotSameAs(subNode);
        TreeNode resultSecondFirstSubSubNode = resultNode.children().get(1).children().get(0);
        assertThat(resultSecondFirstSubSubNode.value()).isEqualTo(charClass("abc"));
        assertThat(resultSecondFirstSubSubNode).isSameAs(resultFirstSubNode);
    }

    private CharClass charClass(String chars) {
        return CharClass.of(chars, charComparator);
    }

}
