package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;

public class SamplerBinomialMonotonicTest extends AbstractMonotonicTest<BinomialMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    
    
    @Override
    protected BinomialMonotonic create(BigInteger size, BigInteger imageSize) {
        return new BinomialMonotonic(treeRandom, size, imageSize);
    }
    
}
