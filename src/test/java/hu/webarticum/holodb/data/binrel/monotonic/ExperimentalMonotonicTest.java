package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.random.HasherTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;

public class ExperimentalMonotonicTest extends AbstractMonotonicTest<ExperimentalMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    

    @Override
    protected ExperimentalMonotonic create(BigInteger size, BigInteger imageSize) {
        return new ExperimentalMonotonic(treeRandom, size, imageSize);
    }
    
}
