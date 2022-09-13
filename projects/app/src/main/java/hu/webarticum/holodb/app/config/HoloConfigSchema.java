package hu.webarticum.holodb.app.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigSchema {

    private final String name;

    private final List<HoloConfigTable> tables;
    
    
    public HoloConfigSchema(
            @JsonProperty("name") String name,
            @JsonProperty("tables") List<HoloConfigTable> tables) {
        this.name = name;
        this.tables = new ArrayList<>(tables);
    }
    

    public String name() {
        return name;
    }
    
    public List<HoloConfigTable> tables() {
        return new ArrayList<>(tables);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("tables", tables)
                .build();
    }
    
    @JsonValue
    public Map<String, Object> jsonValue() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("tables", tables);
        return result;
    }
    
}
