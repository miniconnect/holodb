package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

public interface Source<T> {
    
    public Class<T> type();

    public BigInteger size();
    
    public T get(BigInteger index);
    
}
