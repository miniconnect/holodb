package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

class FeistelNetworkPermutationTest extends AbstractPermutationTest {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");

    private final Hasher hasher = new FastHasher();
    
    
    @Test
    void testZeroLength() {
        FeistelNetworkPermutation permutation = create(0, 1);
        checkSize(permutation, LargeInteger.ONE);
        checkPermutation(permutation);
        assertThat(permutation.at(LargeInteger.ZERO)).as("single item").isEqualTo(LargeInteger.ZERO);
    }

    @Test
    void testSmallInstancesCompletely() {
        for (int blockSize = 1; blockSize <= 10; blockSize++) {
            for (int roundPairs = 0; roundPairs <= 5; roundPairs++) {
                Permutation permutation = create(blockSize, roundPairs);
                checkSize(permutation, LargeInteger.TWO.pow(blockSize));
                checkPermutation(permutation);
            }
        }
    }

    @Test
    void testLargeInstancesPartially() {
        for (int blockSize = 11; blockSize <= 50; blockSize++) {
            for (int roundPairs = 1; roundPairs <= 3; roundPairs++) {
                Permutation permutation = create(blockSize, roundPairs);
                checkSize(permutation, LargeInteger.TWO.pow(blockSize));
                checkProbablyPermutation(permutation);
            }
        }
    }

    
    private FeistelNetworkPermutation create(int blockSize, int roundPairs) {
        return new FeistelNetworkPermutation(treeRandom, blockSize, roundPairs, hasher);
    }
    
}
