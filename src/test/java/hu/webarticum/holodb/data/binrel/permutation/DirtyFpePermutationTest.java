package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class DirtyFpePermutationTest extends AbstractPermutationTest<DirtyFpePermutation> {

    private final byte[] key = "0123456789".getBytes();
    
    
    @Override
    protected DirtyFpePermutation create(BigInteger size) {
        return new DirtyFpePermutation(key, size);
    }

}
