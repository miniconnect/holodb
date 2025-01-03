package hu.webarticum.holodb.core.data.binrel.permutation;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class InMemoryRandomPermutation implements Permutation {
    
    private final LargeInteger size;
    
    private final int[] permutatedValues;
    

    public InMemoryRandomPermutation(TreeRandom treeRandom, LargeInteger size) {
        this(treeRandom, size.intValueExact());
    }

    public InMemoryRandomPermutation(TreeRandom treeRandom, int size) {
        this.size = LargeInteger.of(size);
        permutatedValues = createPermutation(treeRandom, size);
    }
    
    private static int[] createPermutation(TreeRandom treeRandom, int size) {
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = i;
        }
        long randomSeed = ByteBuffer.wrap(treeRandom.getBytes(Long.BYTES)).getLong();
        Random random = new Random(randomSeed);
        shuffleInts(values, random);
        return values;
    }
    
    private static void shuffleInts(int[] values, Random random) {
        for (int i = values.length - 1; i > 1; i--) {
            int j = random.nextInt(i + 1);
            int tmp = values[j];
            values[j] = values[i];
            values[i] = tmp;
        }
    }

    
    public int[] permutatedValues() {
        return Arrays.copyOf(permutatedValues, permutatedValues.length);
    }
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return LargeInteger.of(permutatedValues[index.intValueExact()]);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        int intValue = value.intValueExact();
        for (int i = 0; i < permutatedValues.length; i++) {
            if (permutatedValues[i] == intValue) {
                return LargeInteger.of(i);
            }
        }
        throw new NoSuchElementException();
    }

}
