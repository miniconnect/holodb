package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import hu.webarticum.miniconnect.lang.LargeInteger;

abstract class AbstractPermutationTest {

    protected void checkSize(Permutation permutation, LargeInteger size) {
        assertThat(permutation.size()).as("permutation size").isEqualTo(size);
    }

    protected void checkPermutation(Permutation permutation) {
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
        assertThat(permutation).hasSize(size.intValue());
    }

    protected void checkProbablyPermutation(Permutation permutation) {
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
