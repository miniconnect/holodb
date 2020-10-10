package hu.webarticum.holodb.core.model;

import hu.webarticum.holodb.core.data.source.Source;

public interface Column {

    public <T> Source<T> source(Class<T> type);
    
}
