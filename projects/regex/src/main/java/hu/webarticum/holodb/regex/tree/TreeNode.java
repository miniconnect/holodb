package hu.webarticum.holodb.regex.tree;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeNode {
    
    private final Object value;
    
    private final ImmutableList<TreeNode> children;

    public TreeNode(Object value) {
        this(value, ImmutableList.empty());
    }
    
    public TreeNode(Object value, ImmutableList<TreeNode> children) {
        this.value = value;
        this.children = children;
    }
    
    public Object value() {
        return value;
    }

    public ImmutableList<TreeNode> children() {
        return children;
    }
    
}
