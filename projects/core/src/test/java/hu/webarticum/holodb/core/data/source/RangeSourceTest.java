package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;

class RangeSourceTest {

    @Test
    void testEmpty() {
        RangeSource source = new RangeSource(big(0));
        assertThat(source.size()).isEqualTo(big(0));
    }

    @Test
    void testValues() {
        RangeSource source = new RangeSource(big(12));
        assertThat(source.size()).isEqualTo(big(12));
        assertThat(ImmutableList.fromIterable(Range.until(12)).map(source::get))
                .isEqualTo(ImmutableList.of(bigs(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));
    }

    @Test
    void testValuesWithOffset() {
        RangeSource source = new RangeSource(big(3), big(4));
        assertThat(source.size()).isEqualTo(big(4));
        assertThat(ImmutableList.fromIterable(Range.until(4)).map(source::get))
                .isEqualTo(ImmutableList.of(bigs(3, 4, 5, 6)));
    }

    @Test
    void testFind() {
        RangeSource source = new RangeSource(big(14), big(20));
        Range range = source.find(big(20));
        assertThat(range.from()).isEqualTo(6);
        assertThat(range.until()).isEqualTo(7);
    }

    @Test
    void testFindBetween() {
        RangeSource source = new RangeSource(big(3), big(9));
        Range range = source.findBetween(big(5), true, big(7), false);
        assertThat(range.from()).isEqualTo(2);
        assertThat(range.until()).isEqualTo(4);
    }

    @Test
    void testFindBetweenOut() {
        RangeSource source = new RangeSource(big(8), big(12));
        Range range = source.findBetween(big(12), false, big(50), false);
        assertThat(range.from()).isEqualTo(5);
        assertThat(range.until()).isEqualTo(12);
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
