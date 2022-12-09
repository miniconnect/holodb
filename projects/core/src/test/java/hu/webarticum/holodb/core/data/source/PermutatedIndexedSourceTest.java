package hu.webarticum.holodb.core.data.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.permutation.MockPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutatedIndexedSourceTest {

    @Test
    void testUnmatchingSize() {
        Permutation permutation = MockPermutation.of(1, 0, 3);
        IndexedSource<String> source = new UniqueSource<>("lorem", "ipsum");
        assertThatThrownBy(() -> new PermutatedIndexedSource<>(source, permutation))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testType() {
        Permutation permutation = MockPermutation.of(0);
        IndexedSource<String> source = new UniqueSource<>("xxx");
        PermutatedIndexedSource<String> permutatedSource =
                new PermutatedIndexedSource<>(source, permutation);
        assertThat(permutatedSource.type()).isEqualTo(String.class);
    }

    @Test
    void testSize() {
        Permutation permutation = MockPermutation.of(1, 0);
        IndexedSource<String> source = new UniqueSource<>("aaa", "bbb");
        PermutatedIndexedSource<String> permutatedSource =
                new PermutatedIndexedSource<>(source, permutation);
        assertThat(permutatedSource.size()).isEqualTo(large(2));
    }

    @Test
    void testGet() {
        Permutation permutation = MockPermutation.of(2, 0, 3, 1);
        IndexedSource<String> source = new UniqueSource<>("a", "b", "c", "d");
        PermutatedIndexedSource<String> permutatedSource =
                new PermutatedIndexedSource<>(source, permutation);
        ImmutableList<String> permutatedValues =
                ImmutableList.fromIterable(Range.until(source.size())).map(permutatedSource::get);
        assertThat(permutatedValues).containsExactly("b", "d", "a", "c");
    }

    @Test
    void testFind() {
        Permutation permutation = MockPermutation.of(2, 0, 3, 1);
        IndexedSource<String> source = new UniqueSource<>("a", "b", "c", "d");
        PermutatedIndexedSource<String> permutatedSource =
                new PermutatedIndexedSource<>(source, permutation);
        Selection selection = permutatedSource.find("b");
        assertThat(selection).containsExactly(LargeInteger.ZERO);
    }

    @Test
    void testFindBetween() {
        Permutation permutation = MockPermutation.of(2, 1, 3, 0);
        IndexedSource<String> source = new UniqueSource<>("a", "b", "c", "d");
        PermutatedIndexedSource<String> permutatedSource =
                new PermutatedIndexedSource<>(source, permutation);
        Selection selection = permutatedSource.findBetween("c", true, "x", false);
        assertThat(selection.size()).isEqualTo(large(2));
        assertThat(selection).containsExactly(large(3), LargeInteger.ZERO);
    }

    private static LargeInteger large(int value) {
        return LargeInteger.of(value);
    }

}
