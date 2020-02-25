package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public interface ResizablePermutation extends Permutation {

    public ResizablePermutation resize(BigInteger size);
    
}
