package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;

class DirtyFpePermutationTest extends AbstractPermutationTest<DirtyFpePermutation> {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");
    
    
    @Override
    protected DirtyFpePermutation create(BigInteger size) {
        return new DirtyFpePermutation(treeRandom, size);
    }

}
