package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class RangeSourceTest {

    @Test
    void testEmpty() {
        RangeSource source = new RangeSource(LargeInteger.ZERO);
        assertThat(source.size()).isEqualTo(LargeInteger.ZERO);
    }

    @Test
    void testValues() {
        RangeSource source = new RangeSource(LargeInteger.of(12));
        assertThat(source.size()).isEqualTo(LargeInteger.of(12));
        assertThat(ImmutableList.fromIterable(Range.until(12)).map(source::get))
                .isEqualTo(ImmutableList.of(LargeInteger.arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)));
    }

    @Test
    void testValuesWithOffset() {
        RangeSource source = new RangeSource(LargeInteger.of(3), LargeInteger.of(4));
        assertThat(source.size()).isEqualTo(LargeInteger.of(4));
        assertThat(ImmutableList.fromIterable(Range.until(4)).map(source::get))
                .isEqualTo(ImmutableList.of(LargeInteger.arrayOf(3, 4, 5, 6)));
    }

    @Test
    void testFind() {
        RangeSource source = new RangeSource(LargeInteger.of(14), LargeInteger.of(20));
        Range range = source.find(LargeInteger.of(20));
        assertThat(range.from()).isEqualTo(LargeInteger.of(6));
        assertThat(range.until()).isEqualTo(LargeInteger.of(7));
    }

    @Test
    void testFindBetween() {
        RangeSource source = new RangeSource(LargeInteger.of(3), LargeInteger.of(9));
        Range range = source.findBetween(LargeInteger.of(5), true, LargeInteger.of(7), false);
        assertThat(range.from()).isEqualTo(LargeInteger.of(2));
        assertThat(range.until()).isEqualTo(LargeInteger.of(4));
    }

    @Test
    void testFindBetweenOut() {
        RangeSource source = new RangeSource(LargeInteger.of(8), LargeInteger.of(12));
        Range range = source.findBetween(LargeInteger.of(12), false, LargeInteger.of(50), false);
        assertThat(range.from()).isEqualTo(LargeInteger.of(5));
        assertThat(range.until()).isEqualTo(LargeInteger.of(12));
    }

}
