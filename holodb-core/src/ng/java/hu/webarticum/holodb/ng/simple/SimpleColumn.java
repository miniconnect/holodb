package hu.webarticum.holodb.ng.simple;

import hu.webarticum.holodb.ng.core.Column;
import hu.webarticum.holodb.ng.core.Source;

public class SimpleColumn implements Column {
    
    private final Source<?> source;
    
    
    public SimpleColumn(Source<?> source) {
        this.source = source;
    }
    

    @SuppressWarnings("unchecked")
    @Override
    public <T> Source<T> source(Class<T> type) {
        return (Source<T>) source;
    }

    
    
}
