package hu.webarticum.holodb.ng.core;

import java.util.List;

public interface Index {

    public List<String> columnNames();
    
    public Selectable selectable();
    
}
