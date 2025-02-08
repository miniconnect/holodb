package hu.webarticum.holodb.regex.trie;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.charclass.CharClass;
import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class TrieValueLocatorTest {

    private final CharComparator charComparator = Character::compare;
    
    @Test
    void testSingleEmpty() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(leaf));
        TrieValueLocator locator = new TrieValueLocator();
        assertThat(locator.locate(root, "")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(locator.locate(root, "x")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "lorem")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.ONE));
    }

    @Test
    void testSingleOneLength() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(CharClass.of("d", charComparator), ImmutableList.of(leaf))));
        TrieValueLocator locator = new TrieValueLocator();
        assertThat(locator.locate(root, "a")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "ax")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "d")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(locator.locate(root, "de")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "e")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "d" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.ONE));
    }

    @Test
    void testCharClass() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(CharClass.of("cgtux", charComparator), ImmutableList.of(leaf))));
        TrieValueLocator locator = new TrieValueLocator();
        assertThat(locator.locate(root, "a")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "ax")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "c")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(locator.locate(root, "cx")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "c" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "d")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "dx")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "g")).isEqualTo(FindPositionResult.found(LargeInteger.ONE));
        assertThat(locator.locate(root, "gs")).isEqualTo(FindPositionResult.notFound(LargeInteger.TWO));
        assertThat(locator.locate(root, "g" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.TWO));
        assertThat(locator.locate(root, "h")).isEqualTo(FindPositionResult.notFound(LargeInteger.TWO));
        assertThat(locator.locate(root, "t")).isEqualTo(FindPositionResult.found(LargeInteger.TWO));
        assertThat(locator.locate(root, "ta")).isEqualTo(FindPositionResult.notFound(LargeInteger.THREE));
        assertThat(locator.locate(root, "tu")).isEqualTo(FindPositionResult.notFound(LargeInteger.THREE));
        assertThat(locator.locate(root, "t" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.THREE));
        assertThat(locator.locate(root, "u")).isEqualTo(FindPositionResult.found(LargeInteger.THREE));
        assertThat(locator.locate(root, "w")).isEqualTo(FindPositionResult.notFound(LargeInteger.FOUR));
        assertThat(locator.locate(root, "x")).isEqualTo(FindPositionResult.found(LargeInteger.FOUR));
        assertThat(locator.locate(root, "x" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.FIVE));
        assertThat(locator.locate(root, "y")).isEqualTo(FindPositionResult.notFound(LargeInteger.FIVE));
        assertThat(locator.locate(root, "z")).isEqualTo(FindPositionResult.notFound(LargeInteger.FIVE));
    }

    @Test
    void testOneLengthAlternation() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(
                TrieNode.of(CharClass.of("b", charComparator), ImmutableList.of(leaf)),
                TrieNode.of(CharClass.of("s", charComparator), ImmutableList.of(leaf)),
                TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(leaf))));
        TrieValueLocator locator = new TrieValueLocator();
        assertThat(locator.locate(root, "a")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "ax")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(locator.locate(root, "b")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(locator.locate(root, "bx")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(locator.locate(root, "b" + TrieNode.LEAF_CHAR)).isEqualTo(
                FindPositionResult.notFound(LargeInteger.ONE));
    }

    @Test
    void testComplex() {
        TrieNode leaf = TrieNode.leafOf(charComparator);
        TrieNode x = TrieNode.of(CharClass.of("x", charComparator), ImmutableList.of(leaf));
        TrieNode mx = TrieNode.of(CharClass.of("mx", charComparator), ImmutableList.of(leaf));
        TrieNode y = TrieNode.of(CharClass.of("y", charComparator), ImmutableList.of(mx));
        TrieNode abd = TrieNode.of(CharClass.of("abd", charComparator), ImmutableList.of(x, y));
        TrieNode t = TrieNode.of(CharClass.of("t", charComparator), ImmutableList.of(leaf));
        TrieNode u = TrieNode.of(CharClass.of("u", charComparator), ImmutableList.of(leaf));
        TrieNode as = TrieNode.of(CharClass.of("as", charComparator), ImmutableList.of(leaf, t, u));
        TrieNode eg = TrieNode.of(CharClass.of("eg", charComparator), ImmutableList.of(leaf, as));
        TrieNode p = TrieNode.of(CharClass.of("p", charComparator), ImmutableList.of(leaf));
        TrieNode root = TrieNode.rootOf(charComparator, ImmutableList.of(abd, eg, p));
        TrieValueLocator locator = new TrieValueLocator();
        ImmutableList<String> matchingStrings = ImmutableList.of(
                "ax", "aym", "ayx", "bx", "bym", "byx", "dx", "dym", "dyx",
                "e", "ea", "eat", "eau", "es", "est", "esu",
                "g", "ga", "gat", "gau", "gs", "gst", "gsu",
                "p");
        assertThat(matchingStrings.map(s -> locator.locate(root, s))).allMatch(r -> r.found())
                .extracting(r -> r.position()).isEqualTo(ImmutableList.fill(24, LargeInteger::of).asList());
        char leafChar = TrieNode.LEAF_CHAR;
        ImmutableList<String> unmatchingStrings = ImmutableList.of(
                "", "a", "axe", "ax" + leafChar, "az", "aza", "b", "bxm",
                "c", "cd", "eo", "gsup", "m", "px", "p" + leafChar, "q", "za");
        ImmutableList<Integer> unmatchingPositions = ImmutableList.of(
                0, 0, 1, 1, 3, 3, 3, 4,
                6, 6, 13, 23, 23, 24, 24, 24, 24);
        assertThat(unmatchingStrings.map(s -> locator.locate(root, s))).allMatch(r -> !r.found())
                .extracting(r -> r.position().intValue()).isEqualTo(unmatchingPositions.asList());
    }

}
