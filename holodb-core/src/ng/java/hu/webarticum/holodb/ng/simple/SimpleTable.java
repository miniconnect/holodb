package hu.webarticum.holodb.ng.simple;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hu.webarticum.holodb.ng.core.Column;
import hu.webarticum.holodb.ng.core.Index;
import hu.webarticum.holodb.ng.core.Table;

public class SimpleTable implements Table {

    private final Map<String, Column> columns = new LinkedHashMap<>();
    
    
    @Override
    public List<String> columnNames() {
        return new ArrayList<>(columns.keySet());
    }

    @Override
    public Column column(String name) {
        return columns.get(name);
    }

    @Override
    public List<Index> indices() {
        
        // TODO
        return List.of();
        
    }

    @Override
    public BigInteger size() {
        return columns.isEmpty() ?
                BigInteger.ZERO :
                columns.entrySet().iterator().next().getValue().source(Object.class).size();
    }

}
