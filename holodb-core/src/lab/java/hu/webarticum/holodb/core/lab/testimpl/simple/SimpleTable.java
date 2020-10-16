package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.model.Column;
import hu.webarticum.holodb.core.model.Table;
import hu.webarticum.holodb.core.model.TableIndex;

public class SimpleTable implements Table {

    private final Map<String, Column> columns = new LinkedHashMap<>();
    
    private final List<TableIndex> indices = new ArrayList<>();
    
    
    public void addColumn(String name, Column column) {
        columns.put(name, column);
    }

    public void addIndex(IndexedSource<?> source, String... columnNames) {
        indices.add(new SimpleTableIndex(source, columnNames));
    }
    
    
    @Override
    public List<String> columnNames() {
        return new ArrayList<>(columns.keySet());
    }

    @Override
    public Column column(String name) {
        return columns.get(name);
    }

    @Override
    public List<TableIndex> indices() {
        return new ArrayList<>(indices);
    }

    @Override
    public BigInteger size() {
        return columns.isEmpty() ?
                BigInteger.ZERO :
                columns.entrySet().iterator().next().getValue().size();
    }

}
