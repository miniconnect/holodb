package hu.webarticum.holodb.data.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.data.selection.Range;

class MonotonicValueSourceTest {

    @Test
    void test() {
        MonotonicValueSource<String> source = createBaseSource();
        assertThat(source.at(BigInteger.valueOf(2))).isEqualTo("banana");
        assertThat(source.find("pear")).isEqualTo(Range.fromUntil(4, 7));
        assertThat(source.find("kiwi")).isEqualTo(Range.fromUntil(3, 3));
        assertThat(source.findBetween("banana", true, "pear", true)).isEqualTo(Range.fromUntil(1, 7));
    }
    
    private MonotonicValueSource<String> createBaseSource() {
        SortedSet<String> values = new TreeSet<>(Arrays.asList("apple", "banana", "kiwi", "orange", "pear", "watermelon"));
        SortedValueSource<String> baseSource = new ArraySortedValueSource<>(values);
        Monotonic monotonic = new MockMonotonic(new long[] { 0, 1, 1, 3, 4, 4, 4, 5 }, 6);
        return new MonotonicValueSource<String>(baseSource, monotonic);
    }
    
    
    private static class MockMonotonic implements Monotonic {

        private final long[] values;
        
        private final BigInteger imageSize;
        
        
        private MockMonotonic(long[] values, long imageSize) {
            this.values = values;
            this.imageSize = BigInteger.valueOf(imageSize);
        }
        
        
        @Override
        public BigInteger size() {
            return BigInteger.valueOf(values.length);
        }

        @Override
        public BigInteger at(BigInteger index) {
            return BigInteger.valueOf(values[index.intValue()]);
        }

        @Override
        public Range indicesOf(BigInteger value) {
            long longValue = value.longValue();
            int from = values.length;
            for (int i = 0; i < values.length; i++) {
                int cmp = BigInteger.valueOf(values[i]).compareTo(value);
                if (cmp > 0) {
                    return Range.fromLength(i, 0);
                } else if (cmp == 0) {
                    from = i;
                    break;
                }
            }
            int until = from + 1;
            while (until < values.length && values[until] == longValue) {
                until++;
            }
            return Range.fromUntil(from, until);
        }

        @Override
        public BigInteger imageSize() {
            return imageSize;
        }
        
    }

}
