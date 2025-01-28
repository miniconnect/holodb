package hu.webarticum.holodb.regex.trie;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class TrieNodeTest {

    private final CharComparator charComparator = Character::compare;

    @Test
    void testCharClass() {
        CharClass charClass = CharClass.of("xyz", charComparator);
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode inner = TrieNode.of(charClass, ImmutableList.of(leaf));
        assertThat(inner.charClass()).isEqualTo(charClass);
    }

    @Test
    void testChildren() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        ImmutableList<TrieNode> children = ImmutableList.of(
                TrieNode.of(CharClass.of("a", charComparator), ImmutableList.of(leaf)),
                TrieNode.of(CharClass.of("xy", charComparator), ImmutableList.of(leaf)),
                TrieNode.of(CharClass.of("45", charComparator), ImmutableList.of(leaf)),
                TrieNode.of(CharClass.of("?=", charComparator), ImmutableList.of(leaf)));
        TrieNode root = TrieNode.rootOf(charComparator, children);
        assertThat(root.children()).isEqualTo(children);
    }

    @Test
    void testSingleCharInnerNode() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode inner = TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(leaf));
        assertProperties(inner, LargeInteger.ONE, LargeInteger.ONE, LargeInteger.ONE);
    }

    @Test
    void testRootOnly() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(leaf));
        assertProperties(root, LargeInteger.ONE, LargeInteger.ONE, LargeInteger.ONE);
    }

    @Test
    void testMultipleCharacters() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode inner = TrieNode.of(CharClass.of("abcd", charComparator), ImmutableList.of(leaf));
        assertProperties(inner, LargeInteger.FOUR, LargeInteger.FOUR, LargeInteger.ONE);
    }

    @Test
    void testRootWithInnerMultipleCharacters() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode inner = TrieNode.of(CharClass.of("abcd", charComparator), ImmutableList.of(leaf));
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(inner));
        assertProperties(root, LargeInteger.FOUR, LargeInteger.ONE, LargeInteger.FOUR);
    }

    @Test
    void testAllNodesInComplex() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode wz = TrieNode.of(CharClass.of("wz", charComparator), ImmutableList.of(leaf));
        TrieNode u = TrieNode.of(CharClass.of("u", charComparator), ImmutableList.of(wz));
        TrieNode z = TrieNode.of(CharClass.of("z", charComparator), ImmutableList.of(u));
        TrieNode f = TrieNode.of(CharClass.of("f", charComparator), ImmutableList.of(u));
        TrieNode eqTilde = TrieNode.of(CharClass.of("=~", charComparator), ImmutableList.of(z, f));
        TrieNode oneSeven = TrieNode.of(CharClass.of("17", charComparator), ImmutableList.of(leaf));
        TrieNode y = TrieNode.of(CharClass.of("y", charComparator), ImmutableList.of(leaf));
        TrieNode b = TrieNode.of(CharClass.of("b", charComparator), ImmutableList.of(y));
        TrieNode x = TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(oneSeven, b));
        TrieNode a = TrieNode.of(CharClass.of("a", charComparator), ImmutableList.of(x));
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(eqTilde, a));
        assertProperties(wz, LargeInteger.TWO, LargeInteger.TWO, LargeInteger.ONE);
        assertProperties(u, LargeInteger.TWO, LargeInteger.ONE, LargeInteger.TWO);
        assertProperties(z, LargeInteger.TWO, LargeInteger.ONE, LargeInteger.TWO);
        assertProperties(f, LargeInteger.TWO, LargeInteger.ONE, LargeInteger.TWO);
        assertProperties(eqTilde, LargeInteger.EIGHT, LargeInteger.TWO, LargeInteger.FOUR);
        assertProperties(oneSeven, LargeInteger.TWO, LargeInteger.TWO, LargeInteger.ONE);
        assertProperties(y, LargeInteger.ONE, LargeInteger.ONE, LargeInteger.ONE);
        assertProperties(b, LargeInteger.ONE, LargeInteger.ONE, LargeInteger.ONE);
        assertProperties(x, LargeInteger.THREE, LargeInteger.ONE, LargeInteger.THREE);
        assertProperties(a, LargeInteger.THREE, LargeInteger.ONE, LargeInteger.THREE);
        assertProperties(root, LargeInteger.ELEVEN, LargeInteger.ONE, LargeInteger.ELEVEN);
    }
    
    private void assertProperties(
            TrieNode trieNode,
            LargeInteger expectedSize,
            LargeInteger expectedCharClassSize,
            LargeInteger expectedChildrenFullSize) {
        assertThat(trieNode.size()).as("Size must be equal to " + expectedSize + " " + trieNode.charClass().chars()).isEqualTo(expectedSize);
        assertThat(trieNode.charClassSize()).as("Char class size must be equal to " + expectedCharClassSize)
                .isEqualTo(expectedCharClassSize);
        assertThat(trieNode.childrenFullSize()).as("Children full size must be equal to " + expectedChildrenFullSize)
                .isEqualTo(expectedChildrenFullSize);
    }

}
