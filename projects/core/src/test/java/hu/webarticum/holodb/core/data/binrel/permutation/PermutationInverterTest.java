package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutationInverterTest {

    @Test
    void testEmpty() {
        Permutation permutation = MockPermutation.of();
        Permutation invertedPermutation = new PermutationInverter(permutation);
        assertThat(invertedPermutation.size().intValueExact()).isZero();
        assertThat(invertedPermutation).isEmpty();
        assertThat(invertedPermutation.inverted()).isEmpty();
    }

    @Test
    void testSimple() {
        Permutation permutation = MockPermutation.of(1, 4, 0, 5, 3, 2);
        Permutation invertedPermutation = new PermutationInverter(permutation);
        assertThat(invertedPermutation.size().intValueExact()).isEqualTo(6);
        assertThat(invertedPermutation).containsExactly(larges(2, 0, 5, 4, 1, 3));
        assertThat(invertedPermutation.inverted()).containsExactly(larges(1, 4, 0, 5, 3, 2));
    }

    
    private LargeInteger[] larges(int... values) {
        LargeInteger[] result = new LargeInteger[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = LargeInteger.of(values[i]);
        }
        return result;
    }
    
}
