package hu.webarticum.holodb.metamodel;

import java.util.Arrays;
import java.util.List;

public interface Index {

    public Table getTable();

    public List<Column> getColumns();
    
    public default Selection filter(Object... values) {
        return filter(Arrays.asList(values));
    }
    
    // TODO: map parameter?
    public Selection filter(List<Object> values);
    
}
