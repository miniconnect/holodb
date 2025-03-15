package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.permutation.MockPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutatedSelectionTest {

    @Test
    void testPermutation() {
        Range range = Range.fromUntil(1, 5);
        Permutation permutation = MockPermutation.of(1, 3, 0, 2, 4, 5);
        PermutatedSelection permutatedSelection = new PermutatedSelection(range, permutation);
        assertThat(ImmutableList.of(LargeInteger.arrayOf(0, 1, 2, 3)).map(permutatedSelection::at))
                .isEqualTo(ImmutableList.of(LargeInteger.arrayOf(3, 0, 2, 4)));
        assertThat(permutatedSelection).containsExactly(LargeInteger.arrayOf(3, 0, 2, 4));
        assertThat(permutatedSelection.reverseOrder()).containsExactly(LargeInteger.arrayOf(4, 2, 0, 3));
        assertThat(permutatedSelection.contains(LargeInteger.of(0))).isTrue();
        assertThat(permutatedSelection.contains(LargeInteger.of(1))).isFalse();
        assertThat(permutatedSelection.contains(LargeInteger.of(2))).isTrue();
        assertThat(permutatedSelection.contains(LargeInteger.of(3))).isTrue();
        assertThat(permutatedSelection.contains(LargeInteger.of(4))).isTrue();
        assertThat(permutatedSelection.contains(LargeInteger.of(5))).isFalse();
    }

}
