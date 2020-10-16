package hu.webarticum.holodb.core.lab.testimpl.simple;

import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.core.model.Column;

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
