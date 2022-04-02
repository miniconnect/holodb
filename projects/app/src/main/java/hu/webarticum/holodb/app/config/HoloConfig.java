package hu.webarticum.holodb.app.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfig {

    private final BigInteger seed;

    private final List<HoloConfigSchema> schemas;
    
    
    public HoloConfig(
            @JsonProperty("seed") BigInteger seed,
            @JsonProperty("schemas") List<HoloConfigSchema> schemas) {
        this.seed = seed;
        this.schemas = new ArrayList<>(schemas);
    }
    

    public BigInteger seed() {
        return seed;
    }
    
    public List<HoloConfigSchema> schemas() {
        return new ArrayList<>(schemas);
    }
    
}
