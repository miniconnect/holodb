package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

public interface ValueSource<T> {

    public BigInteger size();
    
    public T at(BigInteger index);
    
}
