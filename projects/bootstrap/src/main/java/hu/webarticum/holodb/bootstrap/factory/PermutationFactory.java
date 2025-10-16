package hu.webarticum.holodb.bootstrap.factory;

import hu.webarticum.holodb.config.HoloConfigColumn.ShuffleQuality;
import hu.webarticum.holodb.core.data.binrel.permutation.BitShufflePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.BitXorPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.FeistelNetworkPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.binrel.permutation.PermutationComposition;
import hu.webarticum.holodb.core.data.binrel.permutation.InMemoryRandomPermutation;
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
                return createSmallVeryHighQualityPermutation(treeRandom, size);
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
        int bitCount = calculateBitCount(size);
        return new BitXorPermutation(treeRandom, bitCount).resized(size);
    }

    private static Permutation createLowQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return new ModuloPermutation(treeRandom, size);
    }

    private static Permutation createMediumQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        int bitCount = calculateBitCount(size);
        return new PermutationComposition(
                new ModuloPermutation(treeRandom, size),
                new BitShufflePermutation(treeRandom, bitCount).resized(size),
                new ModuloPermutation(treeRandom.sub(size), size),
                new BitXorPermutation(treeRandom, bitCount).resized(size));
    }

    private static int calculateBitCount(LargeInteger size) {
        return size.decrement().bitLength();
    }
    
    private static Permutation createHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return createSha256Permutation(treeRandom, size, 2);
    }

    private static Permutation createSmallVeryHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return new InMemoryRandomPermutation(treeRandom, size);
    }

    private static Permutation createVeryHighQualityPermutation(TreeRandom treeRandom, LargeInteger size) {
        return createSha256Permutation(treeRandom, size, 3);
    }
    
    private static Permutation createSha256Permutation(TreeRandom treeRandom, LargeInteger size, int doubleRounds) {
        int bitCount = size.decrement().bitLength();
        byte[] key = treeRandom.getBytes(8);
        Hasher hasher = new Sha256MacHasher(key);
        return new FeistelNetworkPermutation(treeRandom, bitCount, doubleRounds, hasher).resized(size); 
    }

}
