package hu.webarticum.holodb.app.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import hu.webarticum.miniconnect.util.ToStringBuilder;

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
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("seed", seed)
                .add("schemas", schemas)
                .build();
    }
    
    @JsonValue
    public Map<String, Object> jsonValue() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("seed", seed);
        result.put("schemas", schemas);
        return result;
    }
    
}
