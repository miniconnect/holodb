package hu.webarticum.holodb.regex.lab.graph;

import java.util.List;
import java.util.Objects;

import hu.webarticum.holodb.regex.graph.FrozenNode;
import hu.webarticum.treeprinter.TreeNode;
import hu.webarticum.treeprinter.text.ConsoleText;

public class FrozenNodeTreeNode implements TreeNode {
    
    private final FrozenNode frozenNode;
    
    public FrozenNodeTreeNode(FrozenNode frozenNode) {
        this.frozenNode = frozenNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(String.format(
                "[%s, %s] %s: %s",
                frozenNode.length(),
                System.identityHashCode(frozenNode),
                frozenNode.getClass().getSimpleName(),
                Objects.toString(frozenNode.data())));
        return ConsoleText.of(labelBuilder.toString());
    }

    @Override
    public List<TreeNode> children() {
        return frozenNode.children().map(f -> (TreeNode) new FrozenNodeTreeNode(f)).toArrayList();
    }

}
