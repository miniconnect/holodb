package hu.webarticum.holodb.core.data.binrel.monotonic;

import java.math.BigInteger;

class FastMonotonicTest extends AbstractMonotonicTest<FastMonotonic> {

    @Override
    protected FastMonotonic create(BigInteger size, BigInteger imageSize) {
        return new FastMonotonic(size, imageSize);
    }
    
}
