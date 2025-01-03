package hu.webarticum.holodb.core.data.binrel.permutation;

import java.util.BitSet;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitShufflePermutation implements Permutation {

    private final LargeInteger size;

    private final int[] positions;

    private final int[] inversePositions;
    

    public BitShufflePermutation(TreeRandom treeRandom, LargeInteger bitLength) {
        this(new InMemoryRandomPermutation(treeRandom, bitLength));
    }

    public BitShufflePermutation(TreeRandom treeRandom, int bitLength) {
        this(new InMemoryRandomPermutation(treeRandom, bitLength));
    }
    
    public BitShufflePermutation(InMemoryRandomPermutation bitPermutation) {
        this.positions = bitPermutation.permutatedValues();
        this.size = LargeInteger.TWO.pow(positions.length);
        this.inversePositions = createInversePositions(this.positions);
    }
    
    public BitShufflePermutation(int[] positions) {
        this.size = LargeInteger.TWO.pow(positions.length);
        this.positions = positions;
        this.inversePositions = createInversePositions(positions);
    }
    
    private static int[] createInversePositions(int[] positions) {
        int length = positions.length;
        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[positions[i]] = i;
        }
        return result;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return permutateBits(index, inversePositions);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        return permutateBits(value, positions);
    }

    private LargeInteger permutateBits(LargeInteger n, int[] targets) {
        BitSet sourceBitSet = n.toBitSet();
        BitSet targetBitSet = new BitSet(n.bitLength());
        for (int i = sourceBitSet.nextSetBit(0); i >= 0; i = sourceBitSet.nextSetBit(i + 1)) {
            targetBitSet.set(targets[i]);
        }
        return LargeInteger.nonNegativeOf(targetBitSet);
    }

}
