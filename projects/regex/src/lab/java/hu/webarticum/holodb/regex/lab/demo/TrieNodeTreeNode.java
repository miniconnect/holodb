package hu.webarticum.holodb.regex.lab.demo;

import java.util.Collections;
import java.util.List;

import hu.webarticum.holodb.regex.trie.TrieNode;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.treeprinter.text.ConsoleText;

public class TrieNodeTreeNode implements hu.webarticum.treeprinter.TreeNode {
    
    private final TrieNode innerNode;
    
    public TrieNodeTreeNode(TrieNode innerNode) {
        this.innerNode = innerNode;
    }

    @Override
    public ConsoleText content() {
        StringBuilder textBuilder = new StringBuilder();
        if (innerNode.isRoot()) {
            textBuilder.append("ROOT");
        } else if (innerNode.isLeaf()) {
            textBuilder.append("LEAF");
        } else {
            textBuilder.append(innerNode.charClass().chars());
        }
        LargeInteger size = innerNode.size();
        textBuilder.append(" [" + size + "]");
        return ConsoleText.of(textBuilder.toString());
    }

    @Override
    public List<hu.webarticum.treeprinter.TreeNode> children() {
        if (innerNode.isLeaf()) {
            return Collections.emptyList();
        }
        return innerNode.children().map(n -> (hu.webarticum.treeprinter.TreeNode) new TrieNodeTreeNode(n)).asList();
    }
    
}
