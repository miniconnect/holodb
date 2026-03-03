package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class NullPaddedSourceTest {

    @Test
    void testWrongSize() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 2, 3, 4, 5, 6);
        LargeInteger lessSize = LargeInteger.of(4);
        assertThatThrownBy(() -> new NullPaddedSource<>(baseSource, lessSize))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEmpty() {
        SortedSource<Integer> baseSource = new UniqueSource<>();
        NullPaddedSource<Integer> nullPaddedSource = new NullPaddedSource<>(baseSource, LargeInteger.of(0));
        assertThat(nullPaddedSource.size()).isEqualTo(LargeInteger.of(0));
    }

    @Test
    void testNullsOnly() {
        SortedSource<Integer> baseSource = new UniqueSource<>();
        NullPaddedSource<Integer> nullPaddedSource = new NullPaddedSource<>(baseSource, LargeInteger.of(3));
        assertThat(nullPaddedSource.size()).isEqualTo(LargeInteger.of(3));
        assertThat(ImmutableList.fill(
                        nullPaddedSource.size().intValue(),
                        i -> nullPaddedSource.get(LargeInteger.of(i)))
                )
                .containsExactly(null, null, null);
    }

    @Test
    void testNoNulls() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 2, 3, 4, 5);
        NullPaddedSource<Integer> nullPaddedSource = new NullPaddedSource<>(baseSource, LargeInteger.of(5));
        assertThat(nullPaddedSource.size()).isEqualTo(LargeInteger.of(5));
        assertThat(ImmutableList.fill(
                        nullPaddedSource.size().intValue(),
                        i -> nullPaddedSource.get(LargeInteger.of(i)))
                )
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void testPadded() {
        SortedSource<Integer> baseSource = new UniqueSource<>(1, 3, 5, 7);
        NullPaddedSource<Integer> nullPaddedSource = new NullPaddedSource<>(baseSource, LargeInteger.of(11));
        assertThat(nullPaddedSource.size()).isEqualTo(LargeInteger.of(11));
        assertThat(ImmutableList.fill(
                        nullPaddedSource.size().intValue(),
                        i -> nullPaddedSource.get(LargeInteger.of(i)))
                )
                .containsExactly(1, 3, 5, 7, null, null, null, null, null, null, null);
    }

}
