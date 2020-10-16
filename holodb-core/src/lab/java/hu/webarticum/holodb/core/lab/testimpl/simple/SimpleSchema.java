package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hu.webarticum.holodb.core.model.Schema;
import hu.webarticum.holodb.core.model.Table;

public class SimpleSchema implements Schema {

    private final Map<String, Table> tables = new LinkedHashMap<>();
    
    
    public void addTable(String name, Table table) {
        tables.put(name, table);
    }
    
    
    @Override
    public List<String> tableNames() {
        return new ArrayList<>(tables.keySet());
    }

    @Override
    public Table table(String name) {
        return tables.get(name);
    }

}
