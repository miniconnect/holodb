package hu.webarticum.holodb.core.data.binrel.permutation;

import java.util.BitSet;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class BitXorPermutation implements Permutation {

    private final LargeInteger size;

    private final LargeInteger xorMask;
    

    public BitXorPermutation(TreeRandom treeRandom, LargeInteger bitLength) {
        this(treeRandom, bitLength.intValueExact());
    }

    public BitXorPermutation(TreeRandom treeRandom, int bitLength) {
        this.size = LargeInteger.TWO.pow(bitLength);
        this.xorMask = treeRandom.getNumber(size);
    }
    
    public BitXorPermutation(BitSet xorMask, int bitLength) {
        this.size = LargeInteger.TWO.pow(bitLength);
        this.xorMask = LargeInteger.ofUnsigned(xorMask);
    }
    
    public BitXorPermutation(LargeInteger xorMask, int bitLength) {
        this.size = LargeInteger.TWO.pow(bitLength);
        this.xorMask = xorMask;
    }
    
    
    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return xorMask.xor(index);
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        return xorMask.xor(value);
    }

}
