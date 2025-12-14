package hu.webarticum.holodb.core.data.binrel.monotonic;

import hu.webarticum.miniconnect.lang.LargeInteger;

class FastMonotonicTest extends AbstractMonotonicTest<FastMonotonic> {

    @Override
    protected FastMonotonic create(LargeInteger size, LargeInteger imageSize) {
        return new FastMonotonic(size, imageSize);
    }

}
