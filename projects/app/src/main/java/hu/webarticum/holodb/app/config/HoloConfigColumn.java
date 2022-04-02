package hu.webarticum.holodb.app.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfigColumn {
    
    public enum ColumnMode { DEFAULT, COUNTER, FIXED }
    

    private final String name;

    private final Class<?> type;

    private final ColumnMode mode;

    private final List<Object> values = new ArrayList<>();

    
    public HoloConfigColumn(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("mode") ColumnMode mode,
            @JsonProperty("values") List<Object> values) {
        this.name = name;
        this.type = type;
        this.mode = mode == null ? ColumnMode.DEFAULT : mode;
        if (values != null) {
            this.values.addAll(values);
        }
    }
    

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public ColumnMode mode() {
        return mode;
    }

    public List<Object> values() {
        return new ArrayList<>(values);
    }

}
