package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;

class NullPaddedSortedSourceTest {

    @Test
    void testWrongSize() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 2, 3, 4, 5, 6);
        BigInteger lessSize = big(4);
        assertThatThrownBy(() -> new NullPaddedSortedSource<>(baseSource, lessSize))
                .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    void testEmpty() {
        SortedSource<Integer> baseSource = new UniqueSource<>();
        NullPaddedSortedSource<Integer> nullPaddedSource = new NullPaddedSortedSource<>(baseSource, big(0));
        assertThat(nullPaddedSource.size()).isZero();
        assertThat(nullPaddedSource.findBetween(null, true, null, true)).isEmpty();
        assertThat(nullPaddedSource.find(1)).isEmpty();
        assertThat(nullPaddedSource.findNulls()).isEmpty();
    }

    @Test
    void testNullsOnly() {
        SortedSource<Integer> baseSource = new UniqueSource<>();
        NullPaddedSortedSource<Integer> nullPaddedSource = new NullPaddedSortedSource<>(baseSource, big(3));
        assertThat(nullPaddedSource.size()).isEqualTo(3);
        assertThat(ImmutableList.fill(nullPaddedSource.size().intValue(), i -> nullPaddedSource.get(big(i))))
                .containsExactly(null, null, null);
        assertThat(nullPaddedSource.findBetween(null, true, null, true)).isEmpty();
        assertThat(nullPaddedSource.find(1)).isEmpty();
        assertThat(nullPaddedSource.findNulls()).containsExactly(bigs(0, 1, 2));
    }

    @Test
    void testNoNulls() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 2, 3, 4, 5);
        NullPaddedSortedSource<Integer> nullPaddedSource = new NullPaddedSortedSource<>(baseSource, big(5));
        assertThat(nullPaddedSource.size()).isEqualTo(5);
        assertThat(ImmutableList.fill(nullPaddedSource.size().intValue(), i -> nullPaddedSource.get(big(i))))
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(nullPaddedSource.findBetween(null, true, null, true)).containsExactly(bigs(0, 1, 2, 3, 4));
        assertThat(nullPaddedSource.find(1)).containsExactly(bigs(0));
        assertThat(nullPaddedSource.findNulls()).isEmpty();
    }

    @Test
    void testPadded() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 3, 5, 7);
        NullPaddedSortedSource<Integer> nullPaddedSource = new NullPaddedSortedSource<>(baseSource, big(11));
        assertThat(nullPaddedSource.size()).isEqualTo(11);
        assertThat(ImmutableList.fill(nullPaddedSource.size().intValue(), i -> nullPaddedSource.get(big(i))))
                .containsExactly(1, 3, 5, 7, null, null, null, null, null, null, null);
        assertThat(nullPaddedSource.findBetween(null, true, null, true)).containsExactly(bigs(0, 1, 2, 3));
        assertThat(nullPaddedSource.find(1)).containsExactly(bigs(0));
        assertThat(nullPaddedSource.find(2)).isEmpty();
        assertThat(nullPaddedSource.findNulls()).containsExactly(bigs(4, 5, 6, 7, 8, 9, 10));
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
