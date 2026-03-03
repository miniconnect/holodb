package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.LargeInteger;

class UniqueSourceTest {

    @Test
    void testValues() {
        UniqueSource<String> source = createSource();
        assertThat(source).extracting(Source::size).as("size").isEqualTo(LargeInteger.of(6));
        assertThat(source).extracting(s -> s.get(LargeInteger.of(0))).as("at 0").isEqualTo("apple");
        assertThat(source).extracting(s -> s.get(LargeInteger.of(1))).as("at 1").isEqualTo("banana");
        assertThat(source).extracting(s -> s.get(LargeInteger.of(2))).as("at 2").isEqualTo("kiwi");
        assertThat(source).extracting(s -> s.get(LargeInteger.of(3))).as("at 3").isEqualTo("orange");
        assertThat(source).extracting(s -> s.get(LargeInteger.of(4))).as("at 4").isEqualTo("pear");
        assertThat(source).extracting(s -> s.get(LargeInteger.of(5))).as("at 5").isEqualTo("watermelon");
        assertThatThrownBy(() -> source.get(LargeInteger.of(-1))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> source.get(LargeInteger.of(6))).isInstanceOf(ArrayIndexOutOfBoundsException.class);
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
    void testFindBetweenSameExclusive() {
        Range range = createSource().findBetween("banana", false, "banana", false);
        assertThat(range).isEqualTo(Range.fromUntil(1, 1));
    }

    @Test
    void testFindBetweenMinGreater() {
        Range range = createSource().findBetween("pear", true, "banana", true);
        assertThat(range).isEqualTo(Range.fromUntil(4, 4));
    }

    @Test
    void testFindBetweenFromNull() {
        Range range = createSource().findBetween(null, true, "pear", true);
        assertThat(range).isEqualTo(Range.fromUntil(0, 5));
    }

    @Test
    void testFindBetweenUntilNull() {
        Range range = createSource().findBetween("apple", false, null, true);
        assertThat(range).isEqualTo(Range.fromUntil(1, 6));
    }

    @Test
    void testFindBetweenFromNullUntilNull() {
        Range range = createSource().findBetween(null, true, null, true);
        assertThat(range).isEqualTo(Range.fromUntil(0, 6));
    }

    @Test
    void testFindBetweenEmpty() {
        Range range = createSource().findBetween("cherry", true, "kiwi", false);
        assertThat(range).isEqualTo(Range.fromUntil(2, 2));
    }

    @Test
    void testSort() {
        UniqueSource<String> source = new UniqueSource<>("cherry", "apple", "cherry", "orange", "kiwi");
        assertThat(source.size()).isEqualTo(LargeInteger.of(4));
        assertThat(source.get(LargeInteger.of(2))).isEqualTo("kiwi");
    }

    private static UniqueSource<String> createSource() {
        return new UniqueSource<>(
                String.class,
                Arrays.asList("apple", "banana", "kiwi", "orange", "pear", "watermelon"));
    }

    @Test
    void testFindFoundWithCustomComparator() {
        Range range = createSourceWithCustomComparator().find("17");
        assertThat(range).isEqualTo(Range.fromUntil(2, 3));
    }

    @Test
    void testFindBetweenInclusiveWithCustomComparator() {
        Range range = createSourceWithCustomComparator().findBetween("4", true, "243", true);
        assertThat(range).isEqualTo(Range.fromUntil(1, 6));
    }

    @Test
    void testFindBetweenUntilNullWithCustomComparator() {
        Range range = createSourceWithCustomComparator().findBetween("35", false, null, true);
        assertThat(range).isEqualTo(Range.fromUntil(4, 7));
    }

    private static UniqueSource<String> createSourceWithCustomComparator() {
        return new UniqueSource<>(
                String.class,
                Arrays.asList("1", "4", "17", "35", "120", "243", "1000"),
                (s1, s2) -> Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2)));
    }

}
