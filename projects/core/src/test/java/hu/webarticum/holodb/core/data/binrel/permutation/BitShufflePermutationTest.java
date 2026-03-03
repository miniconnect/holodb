package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class BitShufflePermutationTest extends AbstractPermutationTest {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");


    @Test
    void testZeroLength() {
        Permutation permutation = new BitShufflePermutation(new int[0]);
        checkSize(permutation, LargeInteger.ONE);
        checkPermutation(permutation);
        assertThat(permutation.at(LargeInteger.ZERO)).as("single item").isEqualTo(LargeInteger.ZERO);
    }

    @Test
    void testManuallyCreated1() {
        Permutation permutation = new BitShufflePermutation(new int[] {2, 0, 1});
        checkSize(permutation, LargeInteger.of(8));
        assertThat(ImmutableList.fill(8, i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 2, 4, 6, 1, 3, 5, 7);
        assertThat(ImmutableList.fill(8, i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 4, 1, 5, 2, 6, 3, 7);
    }

    @Test
    void testManuallyCreated2() {
        Permutation permutation = new BitShufflePermutation(new int[] {1, 2, 0});
        checkSize(permutation, LargeInteger.of(8));
        assertThat(ImmutableList.fill(8, i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 4, 1, 5, 2, 6, 3, 7);
        assertThat(ImmutableList.fill(8, i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 2, 4, 6, 1, 3, 5, 7);
    }

    @Test
    void testManuallyCreated3() {
        Permutation permutation = new BitShufflePermutation(new int[] {3, 1, 4, 0, 2});
        checkSize(permutation, LargeInteger.of(32));
        assertThat(ImmutableList.of(0, 9, 10, 13, 18, 31).map(i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 9, 3, 25, 6, 31);
        assertThat(ImmutableList.of(0, 1, 13, 25, 31).map(i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(0, 8, 25, 13, 31);
    }

    @Test
    void testSmallInstancesCompletely() {
        for (int blockSize = 1; blockSize <= 10; blockSize++) {
            Permutation permutation = create(blockSize);
            checkSize(permutation, LargeInteger.TWO.pow(blockSize));
            checkPermutation(permutation);
        }
    }

    @Test
    void testLargeInstancesPartially() {
        for (int blockSize = 11; blockSize <= 50; blockSize++) {
            Permutation permutation = create(blockSize);
            checkSize(permutation, LargeInteger.TWO.pow(blockSize));
            checkProbablyPermutation(permutation);
        }
    }

    private BitShufflePermutation create(int blockSize) {
        return new BitShufflePermutation(treeRandom, blockSize);
    }

}
