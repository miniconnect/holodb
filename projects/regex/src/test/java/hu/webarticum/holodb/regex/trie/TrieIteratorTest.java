package hu.webarticum.holodb.regex.trie;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class TrieIteratorTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testSingle() {
        TrieNode rootNode = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(charClass("l"), ImmutableList.of(
                        TrieNode.of(charClass("o"), ImmutableList.of(
                                TrieNode.of(charClass("r"), ImmutableList.of(
                                        TrieNode.of(charClass("e"), ImmutableList.of(
                                                TrieNode.of(charClass("m"), ImmutableList.of(
                                                        TrieNode.leafOf(charComparator)))))))))))));
        Iterable<String> iterable = () -> TrieIterator.fromBeginning(rootNode);
        assertThat(iterable).containsExactly("lorem");
        Iterable<String> explicitIterable = () -> TrieIterator.fromBeginning(rootNode);
        assertThat(explicitIterable).containsExactly("lorem");
    }

    @Test
    void testSimple() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        ImmutableList<TrieNode> leafOnly = ImmutableList.of(leaf);
        TrieNode rootNode = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(charClass("abc"), ImmutableList.of(
                        TrieNode.of(charClass("x"), leafOnly),
                        TrieNode.of(charClass("y"), leafOnly))),
                TrieNode.of(charClass("z"), leafOnly)));
        assertThat((Iterable<String>) () -> TrieIterator.fromBeginning(rootNode))
                .containsExactly("ax", "ay", "bx", "by", "cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.ZERO))
                .containsExactly("ax", "ay", "bx", "by", "cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.ONE))
                .containsExactly("ay", "bx", "by", "cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.TWO))
                .containsExactly("bx", "by", "cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.THREE))
                .containsExactly("by", "cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.FOUR))
                .containsExactly("cx", "cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.FIVE))
                .containsExactly("cy", "z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.SIX))
                .containsExactly("z");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.SEVEN)).isEmpty();
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
        TrieNode rootNode = TrieNode.rootOf(charComparator, ImmutableList.of(eqTilde, a));
        assertThat((Iterable<String>) () -> TrieIterator.fromBeginning(rootNode))
                .containsExactly("=zuw", "=zuz", "=fuw", "=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.ZERO))
                .containsExactly("=zuw", "=zuz", "=fuw", "=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.ONE))
                .containsExactly("=zuz", "=fuw", "=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.TWO))
                .containsExactly("=fuw", "=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.THREE))
                .containsExactly("=fuz", "~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.FOUR))
                .containsExactly("~zuw", "~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.FIVE))
                .containsExactly("~zuz", "~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.SIX))
                .containsExactly("~fuw", "~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.SEVEN))
                .containsExactly("~fuz", "ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.EIGHT))
                .containsExactly("ax1", "ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.NINE))
                .containsExactly("ax7", "axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.TEN))
                .containsExactly("axby");
        assertThat((Iterable<String>) () -> TrieIterator.fromPosition(rootNode, LargeInteger.ELEVEN)).isEmpty();
    }
    
    private CharClass charClass(String chars) {
        return CharClass.of(chars, charComparator);
    }
    
}
