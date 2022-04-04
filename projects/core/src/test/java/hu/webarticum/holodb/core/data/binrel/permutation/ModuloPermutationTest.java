package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;

class ModuloPermutationTest extends AbstractPermutationTest<ModuloPermutation> {

    private final TreeRandom treeRandom = new HasherTreeRandom("0123456789");
    
    
    @Override
    protected ModuloPermutation create(BigInteger size) {
        return new ModuloPermutation(treeRandom, size);
    }

}
