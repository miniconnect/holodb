package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Comparator;

import org.junit.jupiter.api.Test;

class TransformingSortedSourceTest {

    @Test
    void testBasics() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        assertThat(source.type()).isEqualTo(String.class);
        assertThat(source.size()).isEqualTo(big(20));
    }

    @Test
    void testComparator() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = (Comparator<String>) source.comparator();
        assertThat(comparator.compare("5", "12")).isNegative();
    }

    @Test
    void testGet() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        assertThat(source.get(big(15))).isEqualTo("20");
    }

    @Test
    void testPossibleValues() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        assertThat(source.possibleValues().get()).containsExactly(
                "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23", "24");
    }

    @Test
    void testFind() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        assertThat(source.find("20")).containsExactly(big(15));
    }

    @Test
    void testFindBetween() {
        TransformingSortedSource<BigInteger, String> source = createTransformingSortedSource();
        assertThat(source.findBetween("6", true, "12", true)).containsExactly(bigs(1, 2, 3, 4, 5, 6, 7));
    }

    
    private static TransformingSortedSource<BigInteger, String> createTransformingSortedSource() {
        RangeSource rangeSource = new RangeSource(big(5), big(20));
        return new TransformingSortedSource<>(rangeSource, String.class, BigInteger::new, BigInteger::toString);
    }
    
    private static BigInteger big(int value) {
        return BigInteger.valueOf(value);
    }

    private static BigInteger[] bigs(int... values) {
        BigInteger[] result = new BigInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = BigInteger.valueOf(values[i]);
        }
        return result;
    }

}
