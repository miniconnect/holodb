package hu.webarticum.holodb.regex.charclass;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;


class CharClassEntrySetSplitterTest {

    private final CharClassEntrySetSplitter<Integer> splitter = new CharClassEntrySetSplitter<>();

    private final CharComparator comparator = Character::compare;

    @Test
    void testEmpty() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).isEmpty();
    }

    @Test
    void testSingle() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("abc"), 1);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)));
    }

    @Test
    void testTwoSimpleIntersection() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("abcdef"), 1);
        entries.add(charClassOf("defghi"), 2);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 2)),
                entryOf(charClassOf("ghi"), ImmutableList.of(2)));
    }

    @Test
    void testTwoSimpleIntersectionSameValue() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("abcdef"), 1);
        entries.add(charClassOf("defghi"), 1);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 1)),
                entryOf(charClassOf("ghi"), ImmutableList.of(1)));
    }

    @Test
    void testTwoJump() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("abcghi"), 1);
        entries.add(charClassOf("defjkl"), 2);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(2)),
                entryOf(charClassOf("ghi"), ImmutableList.of(1)),
                entryOf(charClassOf("jkl"), ImmutableList.of(2)));
    }

    @Test
    void testContainment() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("def"), 1);
        entries.add(charClassOf("ghi"), 2);
        entries.add(charClassOf("abcdefghijklmno"), 3);
        entries.add(charClassOf("jkl"), 4);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(3)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 3)),
                entryOf(charClassOf("ghi"), ImmutableList.of(2, 3)),
                entryOf(charClassOf("jkl"), ImmutableList.of(3, 4)),
                entryOf(charClassOf("mno"), ImmutableList.of(3)));

    }

    @Test
    void testComplex() {
        SimpleEntryList<CharClass, Integer> entries = new SimpleEntryList<>();
        entries.add(charClassOf("abcd"), 1);
        entries.add(charClassOf("abcd"), 1);
        entries.add(charClassOf("abcd"), 2);
        entries.add(charClassOf("fvwx"), 99);
        entries.add(charClassOf("cdefgh"), 4);
        entries.add(charClassOf("fhi"), 4);
        entries.add(charClassOf("ab"), 5);
        entries.add(charClassOf("afi"), 6);
        entries.add(charClassOf("cdef"), 7);
        entries.add(charClassOf("y"), 7);
        entries.add(charClassOf("z"), 7);
        SimpleEntryList<CharClass, ImmutableList<Integer>> expected = splitter.split(entries);
        assertThat(expected).containsExactly(
                entryOf(charClassOf("a"), ImmutableList.of(1, 1, 2, 5, 6)),
                entryOf(charClassOf("b"), ImmutableList.of(1, 1, 2, 5)),
                entryOf(charClassOf("cd"), ImmutableList.of(1, 1, 2, 4, 7)),
                entryOf(charClassOf("e"), ImmutableList.of(4, 7)),
                entryOf(charClassOf("f"), ImmutableList.of(99, 4, 4, 6, 7)),
                entryOf(charClassOf("g"), ImmutableList.of(4)),
                entryOf(charClassOf("h"), ImmutableList.of(4, 4)),
                entryOf(charClassOf("i"), ImmutableList.of(4, 6)),
                entryOf(charClassOf("vwx"), ImmutableList.of(99)),
                entryOf(charClassOf("y"), ImmutableList.of(7)),
                entryOf(charClassOf("z"), ImmutableList.of(7)));

    }

    private CharClass charClassOf(String chars) {
        return CharClass.of(chars, comparator);
    }

    private <K extends Comparable<K>, V> SimpleEntryList.Entry<K, V> entryOf(K key, V value) {
        return SimpleEntryList.Entry.of(key, value);
    }

}
