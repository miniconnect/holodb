package hu.webarticum.holodb.app.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfigTable {

    private final String name;

    private final List<HoloConfigColumn> columns;
    
    
    public HoloConfigTable(
            @JsonProperty("name") String name,
            @JsonProperty("columns") List<HoloConfigColumn> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
    }
    

    public String name() {
        return name;
    }
    
    public List<HoloConfigColumn> columns() {
        return new ArrayList<>(columns);
    }
    
}
