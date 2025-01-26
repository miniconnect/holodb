package hu.webarticum.holodb.regex.NEW.charclass;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.regex.NEW.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;


class CharClassEntrySetSplitterTest {

    private CharComparator comparator = (a, b) -> Character.compare(a, b);

    @Test
    void testEmpty() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).isEmpty();
    }

    @Test
    void testSingle() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("abc"), 1);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)));
    }

    @Test
    void testTwoSimpleIntersection() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("abcdef"), 1);
        entrySet.add(charClassOf("defghi"), 2);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 2)),
                entryOf(charClassOf("ghi"), ImmutableList.of(2)));
    }

    @Test
    void testTwoSimpleIntersectionSameValue() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("abcdef"), 1);
        entrySet.add(charClassOf("defghi"), 1);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 1)),
                entryOf(charClassOf("ghi"), ImmutableList.of(1)));
    }

    @Test
    void testTwoJump() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("abcghi"), 1);
        entrySet.add(charClassOf("defjkl"), 2);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(1)),
                entryOf(charClassOf("def"), ImmutableList.of(2)),
                entryOf(charClassOf("ghi"), ImmutableList.of(1)),
                entryOf(charClassOf("jkl"), ImmutableList.of(2)));
    }

    @Test
    void testContainment() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("def"), 1);
        entrySet.add(charClassOf("ghi"), 2);
        entrySet.add(charClassOf("abcdefghijklmno"), 3);
        entrySet.add(charClassOf("jkl"), 4);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
        assertThat(expected).containsExactly(
                entryOf(charClassOf("abc"), ImmutableList.of(3)),
                entryOf(charClassOf("def"), ImmutableList.of(1, 3)),
                entryOf(charClassOf("ghi"), ImmutableList.of(2, 3)),
                entryOf(charClassOf("jkl"), ImmutableList.of(3, 4)),
                entryOf(charClassOf("mno"), ImmutableList.of(3)));
        
    }

    @Test
    void testComplex() {
        SortedEntrySet<CharClass, Integer> entrySet = new SortedEntrySet<>();
        entrySet.add(charClassOf("abcd"), 1);
        entrySet.add(charClassOf("abcd"), 1);
        entrySet.add(charClassOf("abcd"), 2);
        entrySet.add(charClassOf("fvwx"), 99);
        entrySet.add(charClassOf("cdefgh"), 4);
        entrySet.add(charClassOf("fhi"), 4);
        entrySet.add(charClassOf("ab"), 5);
        entrySet.add(charClassOf("afi"), 6);
        entrySet.add(charClassOf("cdef"), 7);
        entrySet.add(charClassOf("y"), 7);
        entrySet.add(charClassOf("z"), 7);
        SortedEntrySet<CharClass, ImmutableList<Integer>> expected = CharClassEntrySetSplitter.of(entrySet).split();
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
    
    private <K extends Comparable<K>, V> SortedEntrySet.Entry<K, V> entryOf(K key, V value) {
        return SortedEntrySet.Entry.of(key, value);
    }
    
}
