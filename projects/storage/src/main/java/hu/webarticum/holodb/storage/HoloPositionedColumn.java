package hu.webarticum.holodb.storage;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.rdmsframework.storage.Column;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;

public class HoloPositionedColumn implements Column {
    
    private final String name;
    
    private final ColumnDefinition definition;
    
    private final Source<? extends ImmutableList<?>> source;
    
    private final int position;
    

    public <T> HoloPositionedColumn(
            String name,
            ColumnDefinition definition,
            Source<? extends ImmutableList<?>> source,
            int position) {
        this.name = name;
        this.definition = definition;
        this.source = source;
        this.position = position;
    }
    
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public ColumnDefinition definition() {
        return definition;
    }

    @Override
    public Object get(BigInteger rowIndex) {
        return source.get(rowIndex).get(position);
    }

}
