package hu.webarticum.holodb.regex.lab.graph;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import hu.webarticum.holodb.regex.graph.data.MutableNode;
import hu.webarticum.treeprinter.TreeNode;
import hu.webarticum.treeprinter.text.ConsoleText;

public class MutableNodeTreeNode implements TreeNode {
    
    private final MutableNode mutableNode;
    
    public MutableNodeTreeNode(MutableNode mutableNode) {
        this.mutableNode = mutableNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(mutableNode.getClass().getSimpleName() + ": " + Objects.toString(mutableNode.value));
        return ConsoleText.of(labelBuilder.toString());
    }

    @Override
    public List<TreeNode> children() {
        return mutableNode.children.stream().map(MutableNodeTreeNode::new).collect(Collectors.toList());
    }

}
