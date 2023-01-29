package hu.webarticum.holodb.app.factory;

import hu.webarticum.holodb.app.config.HoloConfigColumn.ShuffleQuality;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.FeistelNetworkPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.binrel.permutation.SmallPermutation;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationFactory {
    
    private static final LargeInteger MAX_SMALL_SIZE = LargeInteger.of(100);

    
    private PermutationFactory() {
        // static class
    }
    

    public static Permutation createPermutation(
            TreeRandom treeRandom, LargeInteger size, ShuffleQuality shuffleQuality) {
        if (shuffleQuality == ShuffleQuality.NOOP || size.isLessThan(LargeInteger.TWO)) {
            return createNoopPermutation(size);
        } else if (shuffleQuality == ShuffleQuality.MEDIUM) {
            return createMediumQualityPermutation(treeRandom, size);
        } else if (shuffleQuality == ShuffleQuality.VERY_LOW) {
            return createVeryLowQualityPermutation(treeRandom, size);
        } else if (shuffleQuality == ShuffleQuality.LOW) {
            return createLowQualityPermutation(treeRandom, size);
        } else if (shuffleQuality == ShuffleQuality.HIGH) {
            if (size.isLessThanOrEqualTo(MAX_SMALL_SIZE)) {
                return createSmallHighQualityPermutation(treeRandom, size);
            } else {
                return createHighQualityPermutation(treeRandom, size);
            }
        } else if (shuffleQuality == ShuffleQuality.VERY_HIGH) {
            if (size.isLessThanOrEqualTo(MAX_SMALL_SIZE)) {
                return createSmallVeryHighQualityPermutation(treeRandom, size);
            } else {
                return createVeryHighQualityPermutation(treeRandom, size);
            }
        } else {
            throw new IllegalArgumentException("Unknown shuffleQuality: " + shuffleQuality);
        }
    }
    
    private static Permutation createNoopPermutation(LargeInteger size) {
        return new IdentityPermutation(size);
    }

    private static Permutation createVeryLowQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return new ModuloPermutation(treeRandom, size);
    }

    private static Permutation createLowQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        // TODO: add additional cheap local shuffling
        return new ModuloPermutation(treeRandom, size);
    }

    private static Permutation createMediumQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        int bitCount = calculateBitCount(size);
        byte[] key = treeRandom.getBytes(4);
        int halfHashByteSize = calculateHalfHashByteSize(bitCount);
        Hasher hasher = new FastHasher(key, halfHashByteSize);
        int doubleRounds = 1;
        return new FeistelNetworkPermutation(treeRandom, bitCount, doubleRounds, hasher).resized(size);   
    }

    private static int calculateBitCount(LargeInteger size) {
        return size.decrement().bitLength();
    }
    
    private static int calculateHalfHashByteSize(int bitCount) {
        return (((bitCount + 7) / 8) + 1) / 2;
    }

    private static Permutation createSmallHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return new SmallPermutation(treeRandom, size);
    }

    private static Permutation createHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        // FIXME: it's slow to initialize
        return new DirtyFpePermutation(treeRandom, size, 6);
    }

    private static Permutation createSmallVeryHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        // TODO: can we improve this in any way?
        return new SmallPermutation(treeRandom, size);
    }

    private static Permutation createVeryHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        int bitCount = size.decrement().bitLength();
        byte[] key = treeRandom.getBytes(8);
        // TODO: scale hash output size
        Hasher hasher = new Sha256MacHasher(key);
        int doubleRounds = 2;
        return new FeistelNetworkPermutation(treeRandom, bitCount, doubleRounds, hasher).resized(size); 
    }

}
