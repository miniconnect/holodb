package hu.webarticum.holodb.core.data.source;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class NullPaddedSource<T> implements Source<T> {
    
    private final Source<T> baseSource;
    
    private final LargeInteger size;
    
    
    public NullPaddedSource(Source<T> baseSource, LargeInteger size) {
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
    public LargeInteger size() {
        return size;
    }

    @Override
    public T get(LargeInteger index) {
        return index.compareTo(baseSource.size()) < 0 ? baseSource.get(index) : null;
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
    }

}
