package hu.webarticum.holodb.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public abstract class AbstractPermutationTest<T extends Permutation> {

    protected abstract T create(BigInteger size);

    
    @Test
    void testEmpty() {
        checkSize(create(BigInteger.ZERO), BigInteger.ZERO);
    }
    
    @Test
    void testSmallInstancesCompletely() {
        for (long i = 10; i <= 100; i += 3) {
            BigInteger size = BigInteger.valueOf(i);
            Permutation permutation = create(size);
            checkSize(permutation, size);
            checkPermutation(permutation);
        }
    }

    @Test
    void testLargeInstancesPartially() {
        BigInteger limit = new BigInteger("1000000000000");
        BigInteger multiplier = BigInteger.valueOf(13L);
        BigInteger incrementum = BigInteger.valueOf(7L);
        for (BigInteger size = BigInteger.valueOf(131L); size.compareTo(limit) <= 0; size = size.multiply(multiplier).add(incrementum)) {
            Permutation permutation = create(size);
            checkSize(permutation, size);
            checkProbablyPermutation(permutation);
        }
    }


    private void checkSize(Permutation permutation, BigInteger size) {
        assertThat(permutation.size()).as("permutation size").isEqualTo(size);
    }

    private void checkPermutation(Permutation permutation) {
        BigInteger size = permutation.size();
        BigInteger max = size.subtract(BigInteger.ONE);
        Set<BigInteger> values = new HashSet<>();
        for (BigInteger index = BigInteger.ZERO; index.compareTo(size) < 0; index = index.add(BigInteger.ONE)) {
            BigInteger value = permutation.at(index);
            values.add(value);
            assertThat(value).as("value range").isBetween(BigInteger.ZERO, max);
            assertThat(permutation.indexOf(value)).as("fetched index").isEqualTo(index);
        }
        assertThat(values).as("collected values").hasSize(size.intValue());
    }

    private void checkProbablyPermutation(Permutation permutation) {
        BigInteger size = permutation.size();
        BigInteger max = size.subtract(BigInteger.ONE);
        int numberOfTests = 20;
        Set<BigInteger> values = new HashSet<>();
        BigInteger numberOfTestsAsBigInteger = BigInteger.valueOf((long) numberOfTests);
        for (int i = 0; i < numberOfTests; i++) {
            BigInteger index = size.multiply(BigInteger.valueOf((long) i)).divide(numberOfTestsAsBigInteger);
            BigInteger value = permutation.at(index);
            values.add(value);
            assertThat(value).as("value range").isBetween(BigInteger.ZERO, max);
            assertThat(permutation.indexOf(value)).as("fetched index").isEqualTo(index);
        }
        assertThat(values).as("collected values").hasSize(numberOfTests);
    }

}
