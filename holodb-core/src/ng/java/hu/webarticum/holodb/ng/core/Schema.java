package hu.webarticum.holodb.ng.core;

import java.util.List;

public interface Schema {

    public List<String> tableNames();
    
    public Table table(String name);
    
}
