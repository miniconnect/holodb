package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class PermutationCompositionTest {

    @Test
    void testNone() {
        assertThatThrownBy(() -> new PermutationComposition()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testNull() {
        assertThatThrownBy(() -> new PermutationComposition(null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void testNonMatchingSize() {
        Permutation permutation1 = new IdentityPermutation(LargeInteger.ONE);
        Permutation permutation2 = new IdentityPermutation(LargeInteger.TWO);
        Permutation permutation3 = new IdentityPermutation(LargeInteger.TEN);
        assertThatThrownBy(() -> new PermutationComposition(permutation1, permutation2, permutation3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSingle() {
        Permutation permutation = MockPermutation.of(2, 3, 1, 0);
        Permutation composition = new PermutationComposition(permutation);
        assertThat(composition.size().intValueExact()).isEqualTo(4);
        assertThat(composition).containsExactly(LargeInteger.arrayOf(2, 3, 1, 0));
        assertThat(composition.inverted()).containsExactly(LargeInteger.arrayOf(3, 2, 0, 1));
    }

    @Test
    void testTwo() {
        Permutation permutation1 = MockPermutation.of(3, 1, 0, 2);
        Permutation permutation2 = MockPermutation.of(0, 2, 3, 1);
        Permutation composition = new PermutationComposition(permutation1, permutation2);
        assertThat(composition.size().intValueExact()).isEqualTo(4);
        assertThat(composition).containsExactly(LargeInteger.arrayOf(3, 0, 2, 1));
        assertThat(composition.inverted()).containsExactly(LargeInteger.arrayOf(1, 3, 2, 0));
    }

    @Test
    void testTwoSymmetric() {
        Permutation permutation1 = MockPermutation.of(2, 3, 1, 0);
        Permutation permutation2 = MockPermutation.of(3, 0, 2, 1);
        Permutation composition = new PermutationComposition(permutation1, permutation2);
        assertThat(composition.size().intValueExact()).isEqualTo(4);
        assertThat(composition).containsExactly(LargeInteger.arrayOf(0, 2, 1, 3));
        assertThat(composition.inverted()).containsExactly(LargeInteger.arrayOf(0, 2, 1, 3));
    }

    @Test
    void testMany() {
        Permutation permutation1 = MockPermutation.of(4, 0, 2, 5, 3, 1);
        Permutation permutation2 = MockPermutation.of(1, 2, 5, 4, 0, 3);
        Permutation permutation3 = MockPermutation.of(3, 5, 4, 0, 2, 1);
        Permutation permutation4 = MockPermutation.of(0, 5, 3, 1, 2, 4);
        Permutation permutation5 = MockPermutation.of(5, 1, 4, 2, 3, 0);
        Permutation composition = new PermutationComposition(
                permutation1, permutation2, permutation3, permutation4, permutation5);
        assertThat(composition.size().intValueExact()).isEqualTo(6);
        assertThat(composition).containsExactly(LargeInteger.arrayOf(1, 2, 4, 0, 5, 3));
        assertThat(composition.inverted()).containsExactly(LargeInteger.arrayOf(3, 0, 1, 5, 2, 4));
    }
    
}
