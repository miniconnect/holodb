package hu.webarticum.holodb.core.model;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.source.Source;

public interface Column {

    public <T> Source<T> source(Class<T> type);

    
    public default Source<?> source() { // NOSONAR
        return source(Object.class);
    }

    public default BigInteger size() {
        return source(Object.class).size();
    }
    
}
