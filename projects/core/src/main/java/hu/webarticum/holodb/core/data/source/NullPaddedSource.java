package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class NullPaddedSource<T> implements Source<T> {
    
    private final Source<T> baseSource;
    
    private final BigInteger size;
    
    
    public NullPaddedSource(Source<T> baseSource, BigInteger size) {
        if (baseSource.size().compareTo(size) > 0) {
            throw new IllegalArgumentException("Base source size can not be larger than target size");
        }
        
        this.baseSource = baseSource;
        this.size = size;
    }
    

    @Override
    public Class<?> type() {
        return baseSource.type();
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public T get(BigInteger index) {
        return index.compareTo(baseSource.size()) < 0 ? baseSource.get(index) : null;
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
    }

}
