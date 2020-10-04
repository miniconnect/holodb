package hu.webarticum.holodb.ng.core;

public interface Column {

    public <T> Source<T> source(Class<T> type);
    
}
