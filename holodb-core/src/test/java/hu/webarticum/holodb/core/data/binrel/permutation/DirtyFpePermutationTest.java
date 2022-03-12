package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

class DirtyFpePermutationTest extends AbstractPermutationTest<DirtyFpePermutation> {

    private final byte[] key = "0123456789".getBytes();
    
    
    @Override
    protected DirtyFpePermutation create(BigInteger size) {
        return new DirtyFpePermutation(key, size);
    }

}
