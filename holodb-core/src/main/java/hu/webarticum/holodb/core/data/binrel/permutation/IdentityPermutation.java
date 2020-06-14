package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

public class IdentityPermutation implements ResizablePermutation {

    private final BigInteger size;
    
    
    public IdentityPermutation(BigInteger size) {
        this.size = size;
    }
    
    
    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public BigInteger at(BigInteger index) {
        return index;
    }

    @Override
    public BigInteger indexOf(BigInteger value) {
        return value;
    }

    @Override
    public ResizablePermutation resize(BigInteger size) {
        return new IdentityPermutation(size);
    }

}
