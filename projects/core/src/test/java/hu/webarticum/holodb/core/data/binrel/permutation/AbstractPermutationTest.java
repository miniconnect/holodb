package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.LargeInteger;

abstract class AbstractPermutationTest<T extends Permutation> {

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


    private void checkSize(Permutation permutation, LargeInteger size) {
        assertThat(permutation.size()).as("permutation size").isEqualTo(size);
    }

    private void checkPermutation(Permutation permutation) {
        LargeInteger size = permutation.size();
        LargeInteger max = size.subtract(LargeInteger.ONE);
        Set<LargeInteger> values = new HashSet<>();
        for (LargeInteger index = LargeInteger.ZERO; index.isLessThan(size); index = index.add(LargeInteger.ONE)) {
            LargeInteger value = permutation.at(index);
            values.add(value);
            assertThat(value).as("value range").isBetween(LargeInteger.ZERO, max);
            assertThat(permutation.indexOf(value)).as("fetched index").isEqualTo(index);
        }
        assertThat(values).as("collected values").hasSize(size.intValue());
    }

    private void checkProbablyPermutation(Permutation permutation) {
        LargeInteger size = permutation.size();
        LargeInteger max = size.subtract(LargeInteger.ONE);
        int numberOfTests = 20;
        Set<LargeInteger> values = new HashSet<>();
        LargeInteger numberOfTestsAsLargeInteger = LargeInteger.of((long) numberOfTests);
        for (int i = 0; i < numberOfTests; i++) {
            LargeInteger index = size.multiply(LargeInteger.of((long) i)).divide(numberOfTestsAsLargeInteger);
            LargeInteger value = permutation.at(index);
            values.add(value);
            assertThat(value).as("value range").isBetween(LargeInteger.ZERO, max);
            assertThat(permutation.indexOf(value)).as("fetched index " + value + " at " + index + " / " + size).isEqualTo(index);
        }
        assertThat(values).as("collected values").hasSize(numberOfTests);
    }

}
