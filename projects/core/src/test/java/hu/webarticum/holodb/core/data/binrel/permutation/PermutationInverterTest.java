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
        assertThat(invertedPermutation).containsExactly(LargeInteger.arrayOf(2, 0, 5, 4, 1, 3));
        assertThat(invertedPermutation.inverted()).containsExactly(LargeInteger.arrayOf(1, 4, 0, 5, 3, 2));
    }
    
}
