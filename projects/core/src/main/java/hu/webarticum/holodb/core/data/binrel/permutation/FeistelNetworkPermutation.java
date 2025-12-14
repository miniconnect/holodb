package hu.webarticum.holodb.core.data.binrel.permutation;

import java.util.BitSet;

import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class FeistelNetworkPermutation implements Permutation {

    private final int blockSize;

    private final int roundPairs;

    private final Hasher hasher;

    private final LargeInteger permutationSize;

    private final BitSet[][] keys;


    public FeistelNetworkPermutation(TreeRandom treeRandom, int blockSize, int roundPairs, Hasher hasher) {
        this.blockSize = blockSize;
        this.roundPairs = roundPairs;
        this.hasher = hasher;
        this.permutationSize = LargeInteger.TWO.pow(blockSize);
        this.keys = createKeys(treeRandom, blockSize, roundPairs);
    }

    private static BitSet[][] createKeys(TreeRandom treeRandom, int blockSize, int roundPairs) {
        int bytes = (blockSize + 1) / 16;
        BitSet[][] keys = new BitSet[roundPairs][];
        for (int i = 0; i < roundPairs; i++) {
            keys[i] = new BitSet[] {
                BitSet.valueOf(treeRandom.sub(i * 2).getBytes(bytes)),
                BitSet.valueOf(treeRandom.sub((i * 2) + 1).getBytes(bytes)),
            };
        }
        return keys;
    }


    @Override
    public LargeInteger size() {
        return permutationSize;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        int leftSize = (blockSize + 1) / 2;
        BitSet[] parts = split(index, leftSize);
        for (int i = 0; i < roundPairs; i++) {
            parts = runDoubleRound(parts, leftSize, keys[i][0], keys[i][1]);
        }
        return join(parts, leftSize);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        int leftSize = blockSize / 2;
        BitSet[] parts = split(value, leftSize);
        for (int i = roundPairs - 1; i >= 0; i--) {
            parts = runDoubleRound(parts, leftSize, keys[i][1], keys[i][0]);
        }
        return join(parts, leftSize);
    }

    private BitSet[] runDoubleRound(BitSet[] parts, int leftSize, BitSet keyA, BitSet keyB) {
        int rightSize = blockSize - leftSize;

        BitSet leftBits = parts[0];
        BitSet rightBits = parts[1];

        BitSet rightHash = hashWithKey(rightBits, keyA);
        BitSet outLeftBits = (BitSet) leftBits.clone();
        outLeftBits.xor(rightHash);
        outLeftBits = outLeftBits.get(0, leftSize);

        BitSet outRightBits = hashWithKey(outLeftBits, keyB);
        outRightBits.xor(rightBits);
        outRightBits = outRightBits.get(0, rightSize);

        return new BitSet[] { outLeftBits, outRightBits };
    }

    private BitSet hashWithKey(BitSet bits, BitSet key) {
        BitSet hashableBits = (BitSet) bits.clone();
        hashableBits.xor(key);
        byte[] hashableBytes = hashableBits.toByteArray();
        byte[] hashBytes = hasher.hash(hashableBytes);
        return BitSet.valueOf(hashBytes);
    }

    private BitSet[] split(LargeInteger value, int leftSize) {
        BitSet allBits = value.toBitSet();
        BitSet leftBits = allBits.get(0, leftSize);
        BitSet rightBits = allBits.get(leftSize, blockSize);
        return new BitSet[] { leftBits, rightBits };
    }

    private LargeInteger join(BitSet[] parts, int leftSize) {
        int rightSize = blockSize - leftSize;
        BitSet leftBits = parts[0];
        BitSet rightBits = parts[1];
        BitSet resultBits = rightBits.get(0, rightSize);
        for (int bi = leftBits.previousSetBit(leftSize); bi != -1; bi = leftBits.previousSetBit(bi - 1)) {
            resultBits.set(rightSize + bi);
        }
        return LargeInteger.ofUnsigned(resultBits);
    }

}
