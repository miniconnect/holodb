package hu.webarticum.holodb.core.data.selection;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.permutation.MockPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.ImmutableList;

class PermutatedSelectionTest {

    @Test
    void testPermutation() {
        Range range = Range.fromUntil(1, 5);
        Permutation permutation = MockPermutation.of(1, 3, 0, 2, 4);
        PermutatedSelection permutatedSelection = new PermutatedSelection(range, permutation);
        assertThat(ImmutableList.of(bigs(0, 1, 2, 3)).map(permutatedSelection::at))
                .isEqualTo(ImmutableList.of(bigs(3, 0, 2, 4)));
        assertThat(permutatedSelection).containsExactly(bigs(3, 0, 2, 4));
        assertThat(permutatedSelection.reverseOrder()).containsExactly(bigs(4, 2, 0, 3));
    }

    private static BigInteger[] bigs(int... values) {
        BigInteger[] result = new BigInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = BigInteger.valueOf(values[i]);
        }
        return result;
    }

}
