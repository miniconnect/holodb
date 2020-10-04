package hu.webarticum.holodb.ng.core;

import java.math.BigInteger;

public interface Selection extends Iterable<BigInteger> {

    public BigInteger size();

    public boolean isEmpty();

    public BigInteger at(BigInteger index);
    
    public boolean contains(BigInteger value);
    
}
