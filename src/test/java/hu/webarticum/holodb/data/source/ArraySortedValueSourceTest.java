package hu.webarticum.holodb.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.data.selection.Range;

class ArraySortedValueSourceTest {

    @Test
    void testValues() {
        ArraySortedValueSource<String> source = createSource();
        assertThat(source.size()).isEqualTo(BigInteger.valueOf(6));
        assertThat(source.at(BigInteger.valueOf(0))).isEqualTo("apple");
        assertThat(source.at(BigInteger.valueOf(1))).isEqualTo("banana");
        assertThat(source.at(BigInteger.valueOf(2))).isEqualTo("kiwi");
        assertThat(source.at(BigInteger.valueOf(3))).isEqualTo("orange");
        assertThat(source.at(BigInteger.valueOf(4))).isEqualTo("pear");
        assertThat(source.at(BigInteger.valueOf(5))).isEqualTo("watermelon");
        assertThatThrownBy(() -> source.at(BigInteger.valueOf(-1))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> source.at(BigInteger.valueOf(6))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
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
        ArraySortedValueSource<String> source = new ArraySortedValueSource<>(Arrays.asList("cherry", "apple", "cherry", "orange", "kiwi"));
        assertThat(source.size()).isEqualTo(BigInteger.valueOf(4));
        assertThat(source.at(BigInteger.valueOf(2))).isEqualTo("kiwi");
    }
    
    private static ArraySortedValueSource<String> createSource() {
        return new ArraySortedValueSource<>(new TreeSet<>(Arrays.asList("apple", "banana", "kiwi", "orange", "pear", "watermelon")));
    }

}
