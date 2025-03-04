package hu.webarticum.holodb.regex.lab.demo;

import java.util.List;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.treeprinter.text.ConsoleText;

public class TreeNodeTreeNode implements hu.webarticum.treeprinter.TreeNode {
    
    private final TreeNode innerNode;
    
    public TreeNodeTreeNode(TreeNode innerNode) {
        this.innerNode = innerNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder labelBuilder = new StringBuilder();
        Object value = innerNode.value();
        String text;
        if (value == null) {
            text = " .-. \n( × )\n `-' ";
        } else if (value instanceof CharClass) {
            text = ((CharClass) value).chars();
        } else {
            text = value.toString();
        }
        labelBuilder.append(text);
        return ConsoleText.of(labelBuilder.toString());
    }

    @Override
    public List<hu.webarticum.treeprinter.TreeNode> children() {
        return innerNode.children().map(n -> (hu.webarticum.treeprinter.TreeNode) new TreeNodeTreeNode(n)).asList();
    }
    
    @Override
    public boolean isDecorable() {
        return innerNode.value() != null;
    }

}
