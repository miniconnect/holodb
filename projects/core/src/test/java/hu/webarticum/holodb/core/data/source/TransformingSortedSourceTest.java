package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class TransformingSortedSourceTest {

    @Test
    void testBasics() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        assertThat(source.type()).isEqualTo(String.class);
        assertThat(source.size()).isEqualTo(large(20));
    }

    @Test
    void testComparator() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = (Comparator<String>) source.comparator();
        assertThat(comparator.compare("5", "12")).isNegative();
    }

    @Test
    void testGet() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        assertThat(source.get(large(15))).isEqualTo("20");
    }

    @Test
    void testPossibleValues() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        assertThat(source.possibleValues().get()).containsExactly(
                "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23", "24");
    }

    @Test
    void testFind() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        assertThat(source.find("20")).containsExactly(large(15));
    }

    @Test
    void testFindBetween() {
        TransformingSortedSource<LargeInteger, String> source = createTransformingSortedSource();
        assertThat(source.findBetween("6", true, "12", true)).containsExactly(larges(1, 2, 3, 4, 5, 6, 7));
    }

    
    private static TransformingSortedSource<LargeInteger, String> createTransformingSortedSource() {
        RangeSource rangeSource = new RangeSource(large(5), large(20));
        return new TransformingSortedSource<>(rangeSource, String.class, LargeInteger::of, LargeInteger::toString);
    }
    
    private static LargeInteger large(int value) {
        return LargeInteger.of(value);
    }

    private static LargeInteger[] larges(int... values) {
        LargeInteger[] result = new LargeInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = LargeInteger.of(values[i]);
        }
        return result;
    }

}
