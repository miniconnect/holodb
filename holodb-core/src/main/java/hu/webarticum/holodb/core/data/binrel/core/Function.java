package hu.webarticum.holodb.core.data.binrel.core;

import java.math.BigInteger;
import java.util.Iterator;

public interface Function extends Iterable<BigInteger> {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
    @Override
    public default Iterator<BigInteger> iterator() {
        return new FunctionIterator(this);
    }
    
}
