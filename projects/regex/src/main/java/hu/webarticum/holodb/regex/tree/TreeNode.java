package hu.webarticum.holodb.regex.tree;

import java.util.Objects;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeNode {
    
    private final Object value;
    
    private final ImmutableList<TreeNode> children;

    
    private TreeNode(Object value, ImmutableList<TreeNode> children) {
        this.value = value;
        this.children = children;
    }

    public static TreeNode of(Object value) {
        return new TreeNode(value, ImmutableList.empty());
    }
    
    public static TreeNode of(Object value, ImmutableList<TreeNode> children) {
        return new TreeNode(value, children);
    }
    
    
    public Object value() {
        return value;
    }

    public ImmutableList<TreeNode> children() {
        return children;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, children);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TreeNode)) {
            return false;
        }
        TreeNode other = (TreeNode) obj;
        return Objects.equals(value, other.value) && children.equals(other.children);
    }
    
}
