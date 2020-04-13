package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

public class FastMonotonicTest extends AbstractMonotonicTest<FastMonotonic> {

    @Override
    protected FastMonotonic create(BigInteger size, BigInteger imageSize) {
        return new FastMonotonic(size, imageSize);
    }
    
}
