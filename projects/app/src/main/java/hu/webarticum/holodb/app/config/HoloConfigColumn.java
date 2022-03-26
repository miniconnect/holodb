package hu.webarticum.holodb.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfigColumn {

    private final String name;

    
    public HoloConfigColumn(@JsonProperty("name") String name) {
        this.name = name;
    }
    
    
    public String name() {
        return name;
    }
    
}
