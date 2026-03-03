package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

class IdentityPermutationTest extends AbstractLargeGroundPermutationTest<IdentityPermutation> {

    @Override
    protected IdentityPermutation create(LargeInteger size) {
        return new IdentityPermutation(size);
    }

    @Test
    void testIdentity() {
        int[] sizes = new int[] { 1, 2, 3, 4, 5, 10, 20, 100, 341 };
        for (int size : sizes) {
            checkIdentity(create(LargeInteger.of(size)));
        }
    }

    @Test
    void testProbablyIdentity() {
        LargeInteger[] sizes = new LargeInteger[] {
            LargeInteger.of("23645728345"),
            LargeInteger.of("7016384293457234"),
            LargeInteger.of("680681618274322737482"),
            LargeInteger.of("7348758239287593045682879568920"),
        };
        for (LargeInteger size : sizes) {
            checkProbablyIdentity(create(size));
        }
    }

    private void checkIdentity(Permutation permutation) {
        LargeInteger size = permutation.size();
        for (LargeInteger index = LargeInteger.ZERO; index.isLessThan(size); index = index.add(LargeInteger.ONE)) {
            LargeInteger value = permutation.at(index);
            assertThat(value).isEqualTo(index);
        }
    }

    private void checkProbablyIdentity(Permutation permutation) {
        int numberOfTests = 20;
        LargeInteger size = permutation.size();
        LargeInteger step = size.divide(LargeInteger.of(numberOfTests));
        for (LargeInteger index = LargeInteger.ZERO; index.isLessThan(size); index = index.add(step)) {
            LargeInteger value = permutation.at(index);
            assertThat(value).isEqualTo(index);
        }
    }

}
