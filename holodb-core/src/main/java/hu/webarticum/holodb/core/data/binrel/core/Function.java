package hu.webarticum.holodb.core.data.binrel.core;

import java.math.BigInteger;

// FIXME: is this a ValueSource<BigInteger> ?
public interface Function {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
}
