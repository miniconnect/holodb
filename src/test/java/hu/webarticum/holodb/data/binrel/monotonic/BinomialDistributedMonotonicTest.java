package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.data.random.DefaultTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;

public class BinomialDistributedMonotonicTest extends AbstractMonotonicTest<BinomialDistributedMonotonic> {

    private TreeRandom treeRandom = new DefaultTreeRandom(0);
    
    
    @Override
    protected BinomialDistributedMonotonic create(BigInteger size, BigInteger imageSize) {
        return new BinomialDistributedMonotonic(treeRandom, size, imageSize);
    }
    
}
