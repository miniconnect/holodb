package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class RangeSourceTest {

    @Test
    void testEmpty() {
        RangeSource source = new RangeSource(large(0));
        assertThat(source.size()).isEqualTo(large(0));
    }

    @Test
    void testValues() {
        RangeSource source = new RangeSource(large(12));
        assertThat(source.size()).isEqualTo(large(12));
        assertThat(ImmutableList.fromIterable(Range.until(12)).map(source::get))
                .isEqualTo(ImmutableList.of(larges(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));
    }

    @Test
    void testValuesWithOffset() {
        RangeSource source = new RangeSource(large(3), large(4));
        assertThat(source.size()).isEqualTo(large(4));
        assertThat(ImmutableList.fromIterable(Range.until(4)).map(source::get))
                .isEqualTo(ImmutableList.of(larges(3, 4, 5, 6)));
    }

    @Test
    void testFind() {
        RangeSource source = new RangeSource(large(14), large(20));
        Range range = source.find(large(20));
        assertThat(range.from()).isEqualTo(6);
        assertThat(range.until()).isEqualTo(7);
    }

    @Test
    void testFindBetween() {
        RangeSource source = new RangeSource(large(3), large(9));
        Range range = source.findBetween(large(5), true, large(7), false);
        assertThat(range.from()).isEqualTo(2);
        assertThat(range.until()).isEqualTo(4);
    }

    @Test
    void testFindBetweenOut() {
        RangeSource source = new RangeSource(large(8), large(12));
        Range range = source.findBetween(large(12), false, large(50), false);
        assertThat(range.from()).isEqualTo(5);
        assertThat(range.until()).isEqualTo(12);
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
