package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.monotonic.SamplerBinomialMonotonic;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;

public class SamplerBinomialMonotonicTest extends AbstractMonotonicTest<SamplerBinomialMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    
    
    @Override
    protected SamplerBinomialMonotonic create(BigInteger size, BigInteger imageSize) {
        return new SamplerBinomialMonotonic(treeRandom, size, imageSize);
    }
    
}
