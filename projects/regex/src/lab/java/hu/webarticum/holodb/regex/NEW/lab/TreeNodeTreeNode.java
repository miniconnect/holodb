package hu.webarticum.holodb.regex.NEW.lab;

import java.util.List;
import java.util.Objects;

import hu.webarticum.holodb.regex.NEW.tree.TreeNode;
import hu.webarticum.treeprinter.text.ConsoleText;

public class TreeNodeTreeNode implements hu.webarticum.treeprinter.TreeNode {
    
    private final TreeNode innerNode;
    
    public TreeNodeTreeNode(TreeNode mutableNode) {
        this.innerNode = mutableNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(innerNode.getClass().getSimpleName() + ": " + Objects.toString(innerNode.value()));
        return ConsoleText.of(labelBuilder.toString());
    }

    @Override
    public List<hu.webarticum.treeprinter.TreeNode> children() {
        return innerNode.children().map(n -> (hu.webarticum.treeprinter.TreeNode) new TreeNodeTreeNode(n)).asList();
    }

}
