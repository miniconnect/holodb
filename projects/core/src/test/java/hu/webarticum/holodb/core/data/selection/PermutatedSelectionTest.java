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
        assertThat(ImmutableList.of(larges(0, 1, 2, 3)).map(permutatedSelection::at))
                .isEqualTo(ImmutableList.of(larges(3, 0, 2, 4)));
        assertThat(permutatedSelection).containsExactly(larges(3, 0, 2, 4));
        assertThat(permutatedSelection.reverseOrder()).containsExactly(larges(4, 2, 0, 3));
        assertThat(permutatedSelection.contains(large(0))).isTrue();
        assertThat(permutatedSelection.contains(large(1))).isFalse();
        assertThat(permutatedSelection.contains(large(2))).isTrue();
        assertThat(permutatedSelection.contains(large(3))).isTrue();
        assertThat(permutatedSelection.contains(large(4))).isTrue();
        assertThat(permutatedSelection.contains(large(5))).isFalse();
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
