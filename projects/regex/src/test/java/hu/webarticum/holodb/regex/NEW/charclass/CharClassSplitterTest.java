package hu.webarticum.holodb.regex.NEW.charclass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.NEW.charclass.CharClassSplitter.Containment;
import hu.webarticum.miniconnect.lang.ImmutableList;

class CharClassSplitterTest {

    @Test
    void testBothEmpty() {
        CharClass leftCharClass = charClassOf("");
        CharClass rightCharClass = charClassOf("");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).isEmpty();
    }

    @Test
    void testLeftEmpty() {
        CharClass leftCharClass = charClassOf("");
        CharClass rightCharClass = charClassOf("abk");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("abk"), Containment.RIGHT));
    }

    @Test
    void testRightEmpty() {
        CharClass leftCharClass = charClassOf("xyz");
        CharClass rightCharClass = charClassOf("");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("xyz"), Containment.LEFT));
    }

    @Test
    void testStrictlyFollow() {
        CharClass leftCharClass = charClassOf("abc");
        CharClass rightCharClass = charClassOf("xyz");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("abc"), Containment.LEFT),
                entryOf(charClassOf("xyz"), Containment.RIGHT));
    }

    @Test
    void testSimpleIntersection() {
        CharClass leftCharClass = charClassOf("abcdef");
        CharClass rightCharClass = charClassOf("defghi");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("abc"), Containment.LEFT),
                entryOf(charClassOf("def"), Containment.BOTH),
                entryOf(charClassOf("ghi"), Containment.RIGHT));
    }

    @Test
    void testSimpleJump() {
        CharClass leftCharClass = charClassOf("abef");
        CharClass rightCharClass = charClassOf("cdgh");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("ab"), Containment.LEFT),
                entryOf(charClassOf("cd"), Containment.RIGHT),
                entryOf(charClassOf("ef"), Containment.LEFT),
                entryOf(charClassOf("gh"), Containment.RIGHT));
    }

    @Test
    void testIntersectionAndJump() {
        CharClass leftCharClass = charClassOf("abefgh");
        CharClass rightCharClass = charClassOf("cdghij");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("ab"), Containment.LEFT),
                entryOf(charClassOf("cd"), Containment.RIGHT),
                entryOf(charClassOf("ef"), Containment.LEFT),
                entryOf(charClassOf("gh"), Containment.BOTH),
                entryOf(charClassOf("ij"), Containment.RIGHT));
    }

    @Test
    void testRightJumpInsideLeft() {
        CharClass leftCharClass = charClassOf("abcdefghij");
        CharClass rightCharClass = charClassOf("cdgh");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("ab"), Containment.LEFT),
                entryOf(charClassOf("cd"), Containment.BOTH),
                entryOf(charClassOf("ef"), Containment.LEFT),
                entryOf(charClassOf("gh"), Containment.BOTH),
                entryOf(charClassOf("ij"), Containment.LEFT));
    }

    @Test
    void testLeftJumpInsideRight() {
        CharClass leftCharClass = charClassOf("cdgh");
        CharClass rightCharClass = charClassOf("abcdefghij");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("ab"), Containment.RIGHT),
                entryOf(charClassOf("cd"), Containment.BOTH),
                entryOf(charClassOf("ef"), Containment.RIGHT),
                entryOf(charClassOf("gh"), Containment.BOTH),
                entryOf(charClassOf("ij"), Containment.RIGHT));
    }

    @Test
    void testComplex() {
        CharClass leftCharClass = charClassOf("abdghijlox");
        CharClass rightCharClass = charClassOf("bghklmpxyz");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("a"), Containment.LEFT),
                entryOf(charClassOf("b"), Containment.BOTH),
                entryOf(charClassOf("d"), Containment.LEFT),
                entryOf(charClassOf("gh"), Containment.BOTH),
                entryOf(charClassOf("ij"), Containment.LEFT),
                entryOf(charClassOf("k"), Containment.RIGHT),
                entryOf(charClassOf("l"), Containment.BOTH),
                entryOf(charClassOf("m"), Containment.RIGHT),
                entryOf(charClassOf("o"), Containment.LEFT),
                entryOf(charClassOf("p"), Containment.RIGHT),
                entryOf(charClassOf("x"), Containment.BOTH),
                entryOf(charClassOf("yz"), Containment.RIGHT));
    }

    @Test
    void testStrictlyPrecede() {
        CharClass leftCharClass = charClassOf("xyz");
        CharClass rightCharClass = charClassOf("abc");
        assertThat(CharClassSplitter.of(leftCharClass, rightCharClass).split()).containsExactly(
                entryOf(charClassOf("abc"), Containment.RIGHT),
                entryOf(charClassOf("xyz"), Containment.LEFT));
    }

    private CharClass charClassOf(String chars) {
        return CharClass.of(ImmutableList.fromCharArray(chars), Comparator.naturalOrder());
    }
    
    private <K extends Comparable<K>, V> SortedEntrySet.Entry<K, V> entryOf(K key, V value) {
        return SortedEntrySet.Entry.of(key, value);
    }
    
}
