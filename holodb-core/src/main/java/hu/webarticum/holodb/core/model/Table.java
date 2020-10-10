package hu.webarticum.holodb.core.model;

import java.math.BigInteger;
import java.util.List;

public interface Table {
    
    public List<String> columnNames();
    
    public Column column(String name);
    
    public List<TableIndex> indices();

    public BigInteger size();
    
}
