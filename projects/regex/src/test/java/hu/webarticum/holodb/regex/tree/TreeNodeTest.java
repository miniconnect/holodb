package hu.webarticum.holodb.regex.tree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class TreeNodeTest {

    @Test
    void testConstructorValueAndChildren() {
        TreeNode treeNode = new TreeNode("123", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        assertThat(treeNode.value()).isEqualTo("123");
        assertThat(treeNode.children()).isEqualTo(ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
    }

    @Test
    void testHashCode() {
        TreeNode treeNode1 = new TreeNode("123", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        TreeNode treeNode2 = new TreeNode("123", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        assertThat(treeNode1).hasSameHashCodeAs(treeNode2);
    }

    @Test
    void testEquals() {
        TreeNode treeNode1 = new TreeNode("123", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        TreeNode treeNode2 = new TreeNode("123", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        TreeNode treeNode3 = new TreeNode("123", ImmutableList.of(new TreeNode("xxxX"), new TreeNode("yyy")));
        TreeNode treeNode4 = new TreeNode("1234", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyy")));
        TreeNode treeNode5 = new TreeNode("1234", ImmutableList.of(new TreeNode("xxx"), new TreeNode("yyyY")));
        assertThat(treeNode1)
                .isEqualTo(treeNode1)
                .isEqualTo(treeNode2)
                .isNotEqualTo(treeNode3)
                .isNotEqualTo(treeNode4)
                .isNotEqualTo(treeNode5)
                .isNotEqualTo(new Object());
    }

}
