package hu.webarticum.holodb.regex.tree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class TreeNodeTest {

    @Test
    void testConstructorValueAndChildren() {
        TreeNode treeNode = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        assertThat(treeNode.value()).isEqualTo("123");
        assertThat(treeNode.children()).isEqualTo(ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
    }

    @Test
    void testHashCode() {
        TreeNode treeNode1 = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        TreeNode treeNode2 = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        assertThat(treeNode1).hasSameHashCodeAs(treeNode2);
    }

    @Test
    void testEquals() {
        TreeNode treeNode1 = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        TreeNode treeNode2 = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        TreeNode treeNode3 = TreeNode.of("123", ImmutableList.of(TreeNode.of("xxxX"), TreeNode.of("yyy")));
        TreeNode treeNode4 = TreeNode.of("1234", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyy")));
        TreeNode treeNode5 = TreeNode.of("1234", ImmutableList.of(TreeNode.of("xxx"), TreeNode.of("yyyY")));
        assertThat(treeNode1)
                .isEqualTo(treeNode1)
                .isEqualTo(treeNode2)
                .isNotEqualTo(treeNode3)
                .isNotEqualTo(treeNode4)
                .isNotEqualTo(treeNode5)
                .isNotEqualTo(new Object());
    }

}
