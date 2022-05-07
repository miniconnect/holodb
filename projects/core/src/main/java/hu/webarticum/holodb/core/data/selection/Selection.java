package hu.webarticum.holodb.core.data.selection;

import java.math.BigInteger;

import hu.webarticum.miniconnect.lang.ReversibleIterable;

public interface Selection extends ReversibleIterable<BigInteger> {

    public BigInteger size();

    public boolean isEmpty();

    public BigInteger at(BigInteger index);
    
    public boolean contains(BigInteger value);
    
}
