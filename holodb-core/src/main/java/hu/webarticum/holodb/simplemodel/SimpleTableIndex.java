package hu.webarticum.holodb.simplemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.model.TableIndex;

public class SimpleTableIndex implements TableIndex {
    
    private final List<String> columnNames;
    
    private final IndexedSource<?> source;
    
    
    public SimpleTableIndex(String columnName, IndexedSource<?> source) {
        this(List.of(columnName), source);
    }

    public SimpleTableIndex(Collection<String> columnNames, IndexedSource<?> source) {
        this.columnNames = new ArrayList<>(columnNames);
        this.source = source;
    }
    

    @Override
    public List<String> columnNames() {
        return new ArrayList<>(columnNames);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Index<T> index(Class<T> type) {
        Class<?> sourceType = source.type();
        if (!sourceType.equals(type)) {
            throw new IllegalArgumentException(
                    String.format("Type error: %s expected but %s given",
                            sourceType, type));
        }
        
        return (Index<T>) source;
    }

}
