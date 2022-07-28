package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.permutation.MockPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.miniconnect.lang.ImmutableList;

class PermutatedSourceTest {

    @Test
    void testUnmatchingSize() {
        Permutation permutation = MockPermutation.of(1, 0, 3);
        Source<String> source = new FixedSource<>("lorem", "ipsum");
        assertThatThrownBy(() -> new PermutatedSource<>(source, permutation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testType() {
        Permutation permutation = MockPermutation.of(0);
        Source<String> source = new FixedSource<>("xxx");
        PermutatedSource<String> permutatedSource =
                new PermutatedSource<>(source, permutation);
        assertThat(permutatedSource.type()).isEqualTo(String.class);
    }

    @Test
    void testSize() {
        Permutation permutation = MockPermutation.of(1, 0);
        Source<String> source = new FixedSource<>("aaa", "bbb");
        PermutatedSource<String> permutatedSource =
                new PermutatedSource<>(source, permutation);
        assertThat(permutatedSource.size()).isEqualTo(2);
    }

    @Test
    void testGet() {
        Permutation permutation = MockPermutation.of(2, 0, 3, 1);
        Source<String> source = new FixedSource<>("a", "b", "c", "d");
        PermutatedSource<String> permutatedSource =
                new PermutatedSource<>(source, permutation);
        ImmutableList<String> permutatedValues =
                ImmutableList.fromIterable(Range.until(source.size())).map(permutatedSource::get);
        assertThat(permutatedValues).containsExactly("b", "d", "a", "c");
    }

    @Test
    void testMultiGet() {
        Permutation permutation = MockPermutation.of(1, 3, 0, 4, 2);
        Source<String> source = new FixedSource<>("a", "a", "b", "b", "b");
        PermutatedSource<String> permutatedSource =
                new PermutatedSource<>(source, permutation);
        ImmutableList<String> permutatedValues =
                ImmutableList.fromIterable(Range.until(source.size())).map(permutatedSource::get);
        assertThat(permutatedValues).containsExactly("b", "a", "b", "a", "b");
    }

}
