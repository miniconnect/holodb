package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;

public interface Source<T> {
    
    public Class<?> type();

    public BigInteger size();
    
    public T get(BigInteger index);

    public Optional<ImmutableList<T>> possibleValues();
    
}
