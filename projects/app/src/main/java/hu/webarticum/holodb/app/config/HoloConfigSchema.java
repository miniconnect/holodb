package hu.webarticum.holodb.app.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    
}
