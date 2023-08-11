package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

class DirtyFpePermutationTest extends AbstractLargeGroundPermutationTest<DirtyFpePermutation> {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");
    
    
    @Override
    protected DirtyFpePermutation create(LargeInteger size) {
        return new DirtyFpePermutation(treeRandom, size);
    }

}
