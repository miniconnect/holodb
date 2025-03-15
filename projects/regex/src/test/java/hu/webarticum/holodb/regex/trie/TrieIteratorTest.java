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

    @Test
    void testHuge1() {
        int numberOfDigits = 45;
        TrieNode leafNode = TrieNode.leafOf(charComparator);
        TrieNode tailNode = TrieNode.of(CharClass.of("u", charComparator), ImmutableList.of(leafNode));
        TrieNode rootNode = TestUtil.generateDigitsTrie(numberOfDigits, ImmutableList.of(tailNode), charComparator);
        assertThat(TestUtil.fetchN(TrieIterator.fromBeginning(rootNode), 5))
                .containsExactly(
                        "000000000000000000000000000000000000000000000u",
                        "000000000000000000000000000000000000000000001u",
                        "000000000000000000000000000000000000000000002u",
                        "000000000000000000000000000000000000000000003u",
                        "000000000000000000000000000000000000000000004u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.ZERO), 5))
                .containsExactly(
                        "000000000000000000000000000000000000000000000u",
                        "000000000000000000000000000000000000000000001u",
                        "000000000000000000000000000000000000000000002u",
                        "000000000000000000000000000000000000000000003u",
                        "000000000000000000000000000000000000000000004u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(543)), 5))
                .containsExactly(
                        "000000000000000000000000000000000000000000543u",
                        "000000000000000000000000000000000000000000544u",
                        "000000000000000000000000000000000000000000545u",
                        "000000000000000000000000000000000000000000546u",
                        "000000000000000000000000000000000000000000547u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(4627385674830448L)), 5))
                .containsExactly(
                        "000000000000000000000000000004627385674830448u",
                        "000000000000000000000000000004627385674830449u",
                        "000000000000000000000000000004627385674830450u",
                        "000000000000000000000000000004627385674830451u",
                        "000000000000000000000000000004627385674830452u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of("2389435082764074219539")), 5))
                .containsExactly(
                        "000000000000000000000002389435082764074219539u",
                        "000000000000000000000002389435082764074219540u",
                        "000000000000000000000002389435082764074219541u",
                        "000000000000000000000002389435082764074219542u",
                        "000000000000000000000002389435082764074219543u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(
                "407437280946329358606983499365637281809474627")), 5))
                .containsExactly(
                        "407437280946329358606983499365637281809474627u",
                        "407437280946329358606983499365637281809474628u",
                        "407437280946329358606983499365637281809474629u",
                        "407437280946329358606983499365637281809474630u",
                        "407437280946329358606983499365637281809474631u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(
                "777777777777777777499999999999999999999999998")), 5))
                .containsExactly(
                        "777777777777777777499999999999999999999999998u",
                        "777777777777777777499999999999999999999999999u",
                        "777777777777777777500000000000000000000000000u",
                        "777777777777777777500000000000000000000000001u",
                        "777777777777777777500000000000000000000000002u");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(
                "999999999999999999999999999999999999999999995")), 5))
                .containsExactly(
                        "999999999999999999999999999999999999999999995u",
                        "999999999999999999999999999999999999999999996u",
                        "999999999999999999999999999999999999999999997u",
                        "999999999999999999999999999999999999999999998u",
                        "999999999999999999999999999999999999999999999u");
    }

    @Test
    void testHuge2() {
        int numberOfDigits = 32;
        TrieNode leafNode = TrieNode.leafOf(charComparator);
        TrieNode tailNode = TrieNode.of(CharClass.of("l", charComparator), ImmutableList.of(leafNode,
                TrieNode.of(CharClass.of("o", charComparator), ImmutableList.of(leafNode,
                        TrieNode.of(CharClass.of("r", charComparator), ImmutableList.of(leafNode,
                                TrieNode.of(CharClass.of("e", charComparator), ImmutableList.of(leafNode,
                                        TrieNode.of(CharClass.of("m", charComparator), ImmutableList.of(
                                                leafNode))))))))));
        TrieNode rootNode = TestUtil.generateDigitsTrie(numberOfDigits, ImmutableList.of(tailNode), charComparator);
        assertThat(TestUtil.fetchN(TrieIterator.fromBeginning(rootNode), 12))
                .containsExactly(
                        "00000000000000000000000000000000l",
                        "00000000000000000000000000000000lo",
                        "00000000000000000000000000000000lor",
                        "00000000000000000000000000000000lore",
                        "00000000000000000000000000000000lorem",
                        "00000000000000000000000000000001l",
                        "00000000000000000000000000000001lo",
                        "00000000000000000000000000000001lor",
                        "00000000000000000000000000000001lore",
                        "00000000000000000000000000000001lorem",
                        "00000000000000000000000000000002l",
                        "00000000000000000000000000000002lo");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.ZERO), 12))
                .containsExactly(
                        "00000000000000000000000000000000l",
                        "00000000000000000000000000000000lo",
                        "00000000000000000000000000000000lor",
                        "00000000000000000000000000000000lore",
                        "00000000000000000000000000000000lorem",
                        "00000000000000000000000000000001l",
                        "00000000000000000000000000000001lo",
                        "00000000000000000000000000000001lor",
                        "00000000000000000000000000000001lore",
                        "00000000000000000000000000000001lorem",
                        "00000000000000000000000000000002l",
                        "00000000000000000000000000000002lo");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(5647281923477L)), 12))
                .containsExactly(
                        "00000000000000000001129456384695lor",
                        "00000000000000000001129456384695lore",
                        "00000000000000000001129456384695lorem",
                        "00000000000000000001129456384696l",
                        "00000000000000000001129456384696lo",
                        "00000000000000000001129456384696lor",
                        "00000000000000000001129456384696lore",
                        "00000000000000000001129456384696lorem",
                        "00000000000000000001129456384697l",
                        "00000000000000000001129456384697lo",
                        "00000000000000000001129456384697lor",
                        "00000000000000000001129456384697lore");
        assertThat(TestUtil.fetchN(TrieIterator.fromPosition(rootNode, LargeInteger.of(
                "284048673091440363199999999999994")), 12))
                .containsExactly(
                        "56809734618288072639999999999998lorem",
                        "56809734618288072639999999999999l",
                        "56809734618288072639999999999999lo",
                        "56809734618288072639999999999999lor",
                        "56809734618288072639999999999999lore",
                        "56809734618288072639999999999999lorem",
                        "56809734618288072640000000000000l",
                        "56809734618288072640000000000000lo",
                        "56809734618288072640000000000000lor",
                        "56809734618288072640000000000000lore",
                        "56809734618288072640000000000000lorem",
                        "56809734618288072640000000000001l");
    }
    
    private CharClass charClass(String chars) {
        return CharClass.of(chars, charComparator);
    }
    
}
