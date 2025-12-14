package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutationTest {

    @Test
    void testInverted() {
        Permutation permutation = MockPermutation.of(2, 3, 0, 4, 1);
        Permutation invertedPermutation = permutation.inverted();
        assertThat(invertedPermutation.size().intValueExact()).isEqualTo(5);
        assertThat(invertedPermutation).containsExactly(LargeInteger.arrayOf(2, 4, 0, 1, 3));
    }

    @Test
    void testExtendedToNonDivisibleSize() {
        Permutation permutation = MockPermutation.of(2, 3, 0, 4, 1);
        Permutation resizedPermutation = permutation.resized(LargeInteger.of(8));
        assertThat(resizedPermutation.size().intValueExact()).isEqualTo(8);
        assertThat(resizedPermutation).containsExactly(LargeInteger.arrayOf(2, 3, 0, 4, 1, 7, 6, 5));
    }

}
