package hu.webarticum.holodb.core.data.binrel.permutation;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

abstract class AbstractGroundPermutationTest<T extends Permutation> extends AbstractPermutationTest {

    protected abstract T create(LargeInteger size);

    
    @Test
    void testEmpty() {
        checkSize(create(LargeInteger.ZERO), LargeInteger.ZERO);
    }
    
    @Test
    void testSmallInstancesCompletely() {
        for (long i = 10; i <= 100; i += 3) {
            LargeInteger size = LargeInteger.of(i);
            Permutation permutation = create(size);
            checkSize(permutation, size);
            checkPermutation(permutation);
        }
    }

    @Test
    void testLargeInstancesPartially() {
        LargeInteger limit = LargeInteger.of("1000000000000");
        LargeInteger multiplier = LargeInteger.of(13L);
        LargeInteger incrementum = LargeInteger.of(7L);
        for (
                LargeInteger size = LargeInteger.of(131L);
                size.compareTo(limit) <= 0;
                size = size.multiply(multiplier).add(incrementum)
        ) {
            Permutation permutation = create(size);
            checkSize(permutation, size);
            checkProbablyPermutation(permutation);
        }
    }

}
