package hu.webarticum.holodb.storage;

import hu.webarticum.miniconnect.rdmsframework.storage.Column;
import hu.webarticum.miniconnect.rdmsframework.storage.ColumnDefinition;

public class HoloSimpleColumn implements Column {
    
    private final String name;
    
    private final ColumnDefinition definition;
    

    public HoloSimpleColumn(String name, ColumnDefinition definition) {
        this.name = name;
        this.definition = definition;
    }
    
    
    @Override
    public String name() {
        return name;
    }

    @Override
    public ColumnDefinition definition() {
        return definition;
    }

}
