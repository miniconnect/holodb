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
        TreeNode treeLeaf = TreeNode.of(SpecialTreeValues.LEAF, ImmutableList.empty());
        TreeNode tree = TreeNode.of(SpecialTreeValues.ROOT, ImmutableList.of(
                TreeNode.of(CharClass.of("a", charComparator), ImmutableList.of(
                        TreeNode.of(CharClass.of("b", charComparator), ImmutableList.of(
                                TreeNode.of(CharClass.of("c", charComparator), ImmutableList.of(treeLeaf)))))),
                        TreeNode.of(CharClass.of("m", charComparator), ImmutableList.of(
                                TreeNode.of(CharClass.of("n", charComparator), ImmutableList.of(treeLeaf)))),
                        TreeNode.of(CharClass.of("x", charComparator), ImmutableList.of(
                                TreeNode.of(CharClass.of("y", charComparator), ImmutableList.of(
                                        treeLeaf,
                                        TreeNode.of(CharClass.of("z", charComparator), ImmutableList.of(treeLeaf))
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
