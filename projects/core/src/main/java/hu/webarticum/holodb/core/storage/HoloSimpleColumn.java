package hu.webarticum.holodb.core.storage;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.rdmsframework.storage.Column;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;

public class HoloSimpleColumn implements Column {
    
    private final String name;
    
    private final ColumnDefinition definition;
    
    private final Source<?> source;
    

    public HoloSimpleColumn(String name, ColumnDefinition definition, Source<?> source) {
        this.name = name;
        this.definition = definition;
        this.source = source;
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
        return source.get(rowIndex);
    }

}
