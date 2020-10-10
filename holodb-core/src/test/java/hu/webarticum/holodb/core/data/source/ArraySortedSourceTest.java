package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;

class ArraySortedSourceTest {

    @Test
    void testValues() {
        ArraySortedSource<String> source = createSource();
        assertThat(source).extracting(Source::size).as("size").isEqualTo(BigInteger.valueOf(6));
        assertThat(source).extracting(s -> s.get(big(0))).as("at 0").isEqualTo("apple");
        assertThat(source).extracting(s -> s.get(big(1))).as("at 1").isEqualTo("banana");
        assertThat(source).extracting(s -> s.get(big(2))).as("at 2").isEqualTo("kiwi");
        assertThat(source).extracting(s -> s.get(big(3))).as("at 3").isEqualTo("orange");
        assertThat(source).extracting(s -> s.get(big(4))).as("at 4").isEqualTo("pear");
        assertThat(source).extracting(s -> s.get(big(5))).as("at 5").isEqualTo("watermelon");
        assertThatThrownBy(() -> source.get(big(-1))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> source.get(big(6))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void testFindFound() {
        Range range = createSource().find("banana");
        assertThat(range).isEqualTo(Range.fromUntil(1, 2));
    }

    @Test
    void testFindNotFound() {
        Range range = createSource().find("cherry");
        assertThat(range).isEqualTo(Range.fromUntil(2, 2));
    }

    @Test
    void testFindBetweenInclusive() {
        Range range = createSource().findBetween("banana", true, "pear", true);
        assertThat(range).isEqualTo(Range.fromUntil(1, 5));
    }

    @Test
    void testFindBetweenExclusive() {
        Range range = createSource().findBetween("banana", false, "pear", false);
        assertThat(range).isEqualTo(Range.fromUntil(2, 4));
    }

    @Test
    void testFindBetweenInclusiveExclusive() {
        Range range = createSource().findBetween("banana", true, "pear", false);
        assertThat(range).isEqualTo(Range.fromUntil(1, 4));
    }

    @Test
    void testFindBetweenExclusiveInclusive() {
        Range range = createSource().findBetween("banana", false, "pear", true);
        assertThat(range).isEqualTo(Range.fromUntil(2, 5));
    }

    @Test
    void testFindBetweenEmpty() {
        Range range = createSource().findBetween("cherry", true, "kiwi", false);
        assertThat(range).isEqualTo(Range.fromUntil(2, 2));
    }

    @Test
    void testSort() {
        ArraySortedSource<String> source = new ArraySortedSource<>("cherry", "apple", "cherry", "orange", "kiwi");
        assertThat(source.size()).isEqualTo(BigInteger.valueOf(4));
        assertThat(source.get(BigInteger.valueOf(2))).isEqualTo("kiwi");
    }
    
    private static ArraySortedSource<String> createSource() {
        return new ArraySortedSource<>(String.class, new TreeSet<>(Arrays.asList("apple", "banana", "kiwi", "orange", "pear", "watermelon")));
    }

    private static BigInteger big(int value) {
        return BigInteger.valueOf(value);
    }

}
