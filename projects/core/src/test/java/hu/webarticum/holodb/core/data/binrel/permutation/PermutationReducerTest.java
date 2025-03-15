package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutationReducerTest {

    @Test
    void testSimple() {
        Permutation permutation = MockPermutation.of(4, 5, 1, 0, 2, 3);
        Permutation reducedPermutation = new PermutationReducer(permutation, LargeInteger.of(3));
        assertThat(reducedPermutation.size().intValueExact()).isEqualTo(3);
        assertThat(reducedPermutation).containsExactly(LargeInteger.arrayOf(2, 0, 1));
        assertThat(reducedPermutation.inverted()).containsExactly(LargeInteger.arrayOf(1, 2, 0));
    }
    
}
