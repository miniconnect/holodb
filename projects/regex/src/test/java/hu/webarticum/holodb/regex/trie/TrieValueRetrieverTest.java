package hu.webarticum.holodb.regex.trie;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class TrieValueRetrieverTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testSingleEmpty() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(leaf));
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(retriever.retrieve(root, LargeInteger.ZERO)).isEmpty();
    }

    @Test
    void testSingleOneLength() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode charNode = TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(leaf));
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(charNode));
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(retriever.retrieve(root, LargeInteger.ZERO)).isEqualTo("x");
    }

    @Test
    void testSingleLonger() {
        TrieNode child = TrieNode.leafOf(charComparator);
        String chars = "loremipsum";
        for (int i = chars.length() - 1; i >= 0; i--) {
            char c = chars.charAt(i);
            child = TrieNode.of(CharClass.of("" + c, charComparator), ImmutableList.of(child));
        }
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(child));
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(retriever.retrieve(root, LargeInteger.ZERO)).isEqualTo("loremipsum");
    }

    @Test
    void testSimpleMultiChar() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode charsNode = TrieNode.of(CharClass.of("abcdef", charComparator), ImmutableList.of(leaf));
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(charsNode));
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(ImmutableList.fill(6, i -> retriever.retrieve(root, LargeInteger.of(i))))
                .containsExactly("a", "b", "c", "d", "e", "f");
    }

    @Test
    void testSimpleAlternation() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        ImmutableList<TrieNode> children = ImmutableList.of(",", "5", "a")
                .map(c -> TrieNode.of(CharClass.of("" + c, charComparator), ImmutableList.of(leaf)));
        TrieNode root = TrieNode.rootOf(charComparator, children);
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(ImmutableList.fill(3, i -> retriever.retrieve(root, LargeInteger.of(i))))
                .containsExactly(",", "5", "a");
    }

    @Test
    void testAlternationWithMultiChar() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        ImmutableList<TrieNode> children = ImmutableList.of("?~", "3", "xyz")
                .map(c -> TrieNode.of(CharClass.of("" + c, charComparator), ImmutableList.of(leaf)));
        TrieNode root = TrieNode.rootOf(charComparator, children);
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(ImmutableList.fill(6, i -> retriever.retrieve(root, LargeInteger.of(i))))
                .containsExactly("?", "~", "3", "x", "y", "z");
    }

    @Test
    void testComplex() {
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
        TrieValueRetriever retriever = new TrieValueRetriever();
        assertThat(ImmutableList.fill(11, i -> retriever.retrieve(root, LargeInteger.of(i))))
                .containsExactly("=zuw", "=zuz", "=fuw", "=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
    }
    
}
