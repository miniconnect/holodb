package hu.webarticum.holodb.query.dummy;

import java.util.LinkedHashMap;
import java.util.Map;

import hu.webarticum.holodb.query.common.ResultRow;

public class DummyResultRow implements ResultRow {

    private final Map<String, Object> data;
    
    
    public DummyResultRow(Map<String, Object> data) {
        this(new LinkedHashMap<>(data), null);
    }
    
    private DummyResultRow(Map<String, Object> data, Object p2) {
        this.data = data;
    }
    
    public static DummyResultRow wrap(Map<String, Object> data) {
        return new DummyResultRow(data, null);
    }
    
    
    @Override
    public Map<String, Object> data() {
        return new LinkedHashMap<>(data);
    }

}
