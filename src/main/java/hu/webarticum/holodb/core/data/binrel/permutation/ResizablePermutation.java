package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

public interface ResizablePermutation extends Permutation {

    public ResizablePermutation resize(BigInteger size);
    
}
