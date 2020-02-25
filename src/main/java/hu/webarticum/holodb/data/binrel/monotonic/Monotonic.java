package hu.webarticum.holodb.data.binrel.monotonic;

import java.math.BigInteger;

import hu.webarticum.holodb.util.Range;

// TODO: separated ReversibleMonotonic interface

public interface Monotonic {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
    public boolean isReversible();

    /** @throws UnsupportedOperationException */
    public Range indicesOf(BigInteger value);

    /** @throws UnsupportedOperationException */
    public BigInteger imageSize(); // FIXME name?
    
}
