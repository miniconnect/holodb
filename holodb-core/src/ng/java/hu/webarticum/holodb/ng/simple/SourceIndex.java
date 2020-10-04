package hu.webarticum.holodb.ng.simple;

import java.util.List;

import hu.webarticum.holodb.ng.core.Index;
import hu.webarticum.holodb.ng.core.Selectable;
import hu.webarticum.holodb.ng.core.Source;

public class SourceIndex implements Index {
    
    private final String columnName;
    
    
    public SourceIndex(String columnName, Source<? extends Comparable<?>> source) {
        this.columnName = columnName;
        
        // TODO: create index
        
    }
    

    @Override
    public List<String> columnNames() {
        return List.of(columnName);
    }

    @Override
    public Selectable selectable() {
        // TODO Auto-generated method stub
        return null;
    }

}
