package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

class ModuloPermutationTest extends AbstractPermutationTest<ModuloPermutation> {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");
    
    
    @Override
    protected ModuloPermutation create(LargeInteger size) {
        return new ModuloPermutation(treeRandom, size);
    }

}
