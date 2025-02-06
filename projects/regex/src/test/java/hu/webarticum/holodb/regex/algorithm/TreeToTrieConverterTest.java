package hu.webarticum.holodb.regex.algorithm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.tree.TreeNode;
import hu.webarticum.holodb.regex.trie.TrieNode;
import hu.webarticum.miniconnect.lang.ImmutableList;

class TreeToTrieConverterTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testSomeConversion() {
        TreeToTrieConverter converter = new TreeToTrieConverter(charComparator);
        TreeNode treeLeaf = new TreeNode(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode tree = new TreeNode(SpecialTreeValues.ROOT, ImmutableList.of(
                new TreeNode(CharClass.of("a", charComparator), ImmutableList.of(
                        new TreeNode(CharClass.of("b", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("c", charComparator), ImmutableList.of(treeLeaf)))))),
                        new TreeNode(CharClass.of("m", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("n", charComparator), ImmutableList.of(treeLeaf)))),
                        new TreeNode(CharClass.of("x", charComparator), ImmutableList.of(
                                new TreeNode(CharClass.of("y", charComparator), ImmutableList.of(
                                        treeLeaf,
                                        new TreeNode(CharClass.of("z", charComparator), ImmutableList.of(treeLeaf))
        ))))));
        TrieNode trieLeaf = TrieNode.leafOf(charComparator);
        TrieNode trie = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(CharClass.of("a", charComparator), ImmutableList.of(
                        TrieNode.of(CharClass.of("b", charComparator), ImmutableList.of(
                                TrieNode.of(CharClass.of("c", charComparator), ImmutableList.of(trieLeaf)))))),
                        TrieNode.of(CharClass.of("m", charComparator), ImmutableList.of(
                                TrieNode.of(CharClass.of("n", charComparator), ImmutableList.of(trieLeaf)))),
                        TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(
                                TrieNode.of(CharClass.of("y", charComparator), ImmutableList.of(
                                        trieLeaf,
                                        TrieNode.of(CharClass.of("z", charComparator), ImmutableList.of(trieLeaf))
        ))))));
        assertThat(converter.convert(tree)).isEqualTo(trie);
    }
    
}
