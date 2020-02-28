package hu.webarticum.holodb.metamodel;

import java.util.Arrays;
import java.util.List;

public interface Index {

    public List<Column> getColumns();
    
    public default Object filter(Object... values) {
        return filter(Arrays.asList(values));
    }
    
    // TODO: return value? Collection? something like 'IdSet'?
    // TODO: map parameter?
    public Object filter(List<Object> values);
    
}
