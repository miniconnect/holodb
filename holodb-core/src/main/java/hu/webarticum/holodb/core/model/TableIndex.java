package hu.webarticum.holodb.core.model;

import java.util.List;

import hu.webarticum.holodb.core.data.source.Index;

public interface TableIndex {

    public List<String> columnNames();
    
    public <T> Index<T> index(Class<T> type);
    
}
