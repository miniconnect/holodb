package hu.webarticum.holodb.core.data.binrel.permutation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Random;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class SmallPermutation implements Permutation {
    
    private final LargeInteger size;
    
    private final LargeInteger[] permutatedValues;
    

    public SmallPermutation(TreeRandom treeRandom, LargeInteger size) {
        this(treeRandom, size.intValueExact());
    }

    public SmallPermutation(TreeRandom treeRandom, int size) {
        this.size = LargeInteger.of(size);
        permutatedValues = createPermutation(treeRandom, size);
    }
    
    private static LargeInteger[] createPermutation(TreeRandom treeRandom, int size) {
        LargeInteger[] values = new LargeInteger[size];
        for (int i = 0; i < size; i++) {
            values[i] = LargeInteger.of(i);
        }
        long randomSeed = ByteBuffer.wrap(treeRandom.getBytes(Long.BYTES)).getLong();
        Random random = new Random(randomSeed);
        Collections.shuffle(Arrays.asList(values), random);
        return values;
    }

    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return permutatedValues[index.intValueExact()];
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        for (int i = 0; i < permutatedValues.length; i++) {
            if (permutatedValues[i].equals(value)) {
                return LargeInteger.of(i);
            }
        }
        throw new NoSuchElementException();
    }

}
