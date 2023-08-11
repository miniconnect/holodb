package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

class InMemoryRandomPermutationTest extends AbstractGroundPermutationTest<InMemoryRandomPermutation> {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");
    

    @Override
    protected InMemoryRandomPermutation create(LargeInteger size) {
        return new InMemoryRandomPermutation(treeRandom, size);
    }
    
}
