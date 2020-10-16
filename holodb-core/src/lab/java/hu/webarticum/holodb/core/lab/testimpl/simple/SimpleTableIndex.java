package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.model.TableIndex;

public class SimpleTableIndex implements TableIndex {
    
    private final IndexedSource<?> source;
    
    private final List<String> columnNames;
    
    
    public SimpleTableIndex(IndexedSource<?> source, String... columnNames) {
        this(source, Arrays.asList(columnNames));
    }

    public SimpleTableIndex(IndexedSource<?> source, Collection<String> columnNames) {
        this.source = source;
        this.columnNames = new ArrayList<>(columnNames);
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
