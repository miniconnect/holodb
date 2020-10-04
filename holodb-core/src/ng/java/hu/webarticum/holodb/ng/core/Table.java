package hu.webarticum.holodb.ng.core;

import java.math.BigInteger;
import java.util.List;

public interface Table {
    
    public List<String> columnNames();
    
    public Column column(String name);
    
    public List<Index> indices();

    public BigInteger size();
    
}
