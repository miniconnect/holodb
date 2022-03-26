package hu.webarticum.holodb.app.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfig {

    private final List<HoloConfigSchema> schemas;
    
    
    public HoloConfig(@JsonProperty("schemas") List<HoloConfigSchema> schemas) {
        this.schemas = new ArrayList<>(schemas);
    }
    
    
    public List<HoloConfigSchema> schemas() {
        return new ArrayList<>(schemas);
    }
    
}
