package hu.webarticum.holodb.core.model;

import java.util.List;

public interface Schema {

    public List<String> tableNames();
    
    public Table table(String name);
    
}
