package hu.webarticum.holodb.regex.algorithm;

import java.util.IdentityHashMap;
import java.util.Map;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.holodb.regex.trie.TrieNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class TreeToTrieConverter {

    private final CharComparator charComparator;

    public TreeToTrieConverter(CharComparator charComparator) {
        this.charComparator = charComparator;
    }

    public TrieNode convert(TreeNode treeNode) {
        return convertCaching(treeNode, new IdentityHashMap<>());
    }

    public TrieNode convertCaching(TreeNode treeNode, Map<TreeNode, TrieNode> cache) {
        TrieNode cachedTrieNode = cache.get(treeNode);
        if (cachedTrieNode != null) {
            return cachedTrieNode;
        }
        TrieNode convertedTrieNode = convertInternal(treeNode, cache);
        cache.put(treeNode, convertedTrieNode);
        return convertedTrieNode;
    }

    public TrieNode convertInternal(TreeNode treeNode, Map<TreeNode, TrieNode> cache) {
        Object value = treeNode.value();
        if (value == SpecialTreeValues.LEAF) {
            return TrieNode.leafOf(charComparator);
        }
        ImmutableList<TrieNode> convertedChilden = treeNode.children().map(n -> convertCaching(n, cache));
        if (value == SpecialTreeValues.ROOT) {
            return TrieNode.rootOf(charComparator, convertedChilden);
        } else if (value instanceof CharClass) {
            CharClass charClass = (CharClass) value;
            return TrieNode.of(charClass, convertedChilden);
        } else {
            throw new IllegalArgumentException("Unknown value type: " + value);
        }
    }

}
