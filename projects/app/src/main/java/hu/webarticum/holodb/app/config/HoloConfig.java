package hu.webarticum.holodb.app.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfig {

    private final LargeInteger seed;

    private final ImmutableList<HoloConfigSchema> schemas;
    
    
    public HoloConfig(
            @JsonProperty("seed") LargeInteger seed,
            @JsonProperty("schemas") ImmutableList<HoloConfigSchema> schemas) {
        this.seed = seed != null ? seed : LargeInteger.ZERO;
        this.schemas = schemas != null ? schemas : ImmutableList.empty();
    }
    

    @JsonGetter("seed")
    public LargeInteger seed() {
        return seed;
    }

    @JsonGetter("schemas")
    public ImmutableList<HoloConfigSchema> schemas() {
        return schemas;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("seed", seed)
                .add("schemas", schemas)
                .build();
    }
    
}
