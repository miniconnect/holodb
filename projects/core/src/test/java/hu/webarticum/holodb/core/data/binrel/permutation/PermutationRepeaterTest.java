package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutationRepeaterTest {

    @Test
    void testNonDivisible() {
        Permutation permutation = MockPermutation.of(2, 3, 0, 4, 1);
        assertThatThrownBy(() -> new PermutationRepeater(permutation, LargeInteger.of(7)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDivisible() {
        Permutation permutation = MockPermutation.of(3, 0, 1, 2);
        Permutation extendedPermutation = new PermutationRepeater(permutation, LargeInteger.of(8));
        assertThat(extendedPermutation.size().intValueExact()).isEqualTo(8);
        assertThat(extendedPermutation).containsExactly(larges(3, 0, 1, 2, 7, 4, 5, 6));
        assertThat(extendedPermutation.inverted()).containsExactly(larges(1, 2, 3, 0, 5, 6, 7, 4));
    }

    
    private LargeInteger[] larges(int... values) {
        LargeInteger[] result = new LargeInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = LargeInteger.of(values[i]);
        }
        return result;
    }
    
}
