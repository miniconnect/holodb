package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

class SamplerBinomialMonotonicTest extends AbstractMonotonicTest<BinomialMonotonic> {

    private TreeRandom treeRandom = new HasherTreeRandom();
    
    
    @Override
    protected BinomialMonotonic create(LargeInteger size, LargeInteger imageSize) {
        return new BinomialMonotonic(treeRandom, size, imageSize);
    }
    
}
