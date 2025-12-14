package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.BitSet;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class BitXorPermutationTest extends AbstractPermutationTest {

    private final TreeRandom treeRandom = new HasherTreeRandom(423);


    @Test
    void testZeroLength() {
        Permutation permutation = new BitXorPermutation(new BitSet(), 0);
        checkSize(permutation, LargeInteger.ONE);
        checkPermutation(permutation);
        assertThat(permutation.at(LargeInteger.ZERO)).as("single item").isEqualTo(LargeInteger.ZERO);
    }

    @Test
    void testManuallyCreated1() {
        Permutation permutation = new BitXorPermutation(BitSet.valueOf(new long[] { 2 }), 3);
        checkSize(permutation, LargeInteger.of(8));
        assertThat(ImmutableList.fill(8, i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(2, 3, 0, 1, 6, 7, 4, 5);
        assertThat(ImmutableList.fill(8, i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(2, 3, 0, 1, 6, 7, 4, 5);
    }

    @Test
    void testManuallyCreated2() {
        Permutation permutation = new BitXorPermutation(BitSet.valueOf(new long[] { 14 }), 4);
        checkSize(permutation, LargeInteger.of(16));
        assertThat(ImmutableList.fill(16, i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(14, 15, 12, 13, 10, 11, 8, 9, 6, 7, 4, 5, 2, 3, 0, 1);
        assertThat(ImmutableList.fill(16, i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(14, 15, 12, 13, 10, 11, 8, 9, 6, 7, 4, 5, 2, 3, 0, 1);
    }

    @Test
    void testManuallyCreated3() {
        Permutation permutation = new BitXorPermutation(BitSet.valueOf(new long[] { 150 }), 8);
        checkSize(permutation, LargeInteger.of(256));
        assertThat(ImmutableList.of(0, 85, 88, 105, 150, 195, 206, 255).map(i -> permutation.at(LargeInteger.of(i)).intValueExact()))
                .containsExactly(150, 195, 206, 255, 0, 85, 88, 105);
        assertThat(ImmutableList.of(0, 85, 88, 105, 150, 195, 206, 255).map(i -> permutation.indexOf(LargeInteger.of(i)).intValueExact()))
                .containsExactly(150, 195, 206, 255, 0, 85, 88, 105);
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

    private BitXorPermutation create(int blockSize) {
        return new BitXorPermutation(treeRandom, blockSize);
    }

}
