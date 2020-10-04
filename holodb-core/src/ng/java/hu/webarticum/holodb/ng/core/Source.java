package hu.webarticum.holodb.ng.core;

import java.math.BigInteger;

public interface Source<T> {

    public BigInteger size();
    
    public T get(BigInteger index);
    
}
