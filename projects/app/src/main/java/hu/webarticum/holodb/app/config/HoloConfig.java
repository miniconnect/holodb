package hu.webarticum.holodb.app.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfig {

    private final LargeInteger seed;
    
    private final HoloConfigSchema schemaDefaults;
    
    private final HoloConfigTable tableDefaults;

    private final HoloConfigColumn columnDefaults;

    private final ImmutableList<HoloConfigSchema> schemas;
    
    
    public HoloConfig(
            @JsonProperty("seed") LargeInteger seed,
            @JsonProperty("schemaDefaults") HoloConfigSchema schemaDefaults,
            @JsonProperty("tableDefaults") HoloConfigTable tableDefaults,
            @JsonProperty("columnDefaults") HoloConfigColumn columnDefaults,
            @JsonProperty("schemas") ImmutableList<HoloConfigSchema> schemas) {
        this.seed = seed != null ? seed : LargeInteger.ZERO;
        this.schemaDefaults = schemaDefaults;
        this.tableDefaults = tableDefaults;
        this.columnDefaults = columnDefaults;
        this.schemas = schemas != null ? schemas : ImmutableList.empty();
    }
    

    @JsonGetter("seed")
    public LargeInteger seed() {
        return seed;
    }

    @JsonGetter("schemaDefaults")
    public HoloConfigSchema schemaDefaults() {
        return schemaDefaults;
    }

    @JsonGetter("tableDefaults")
    public HoloConfigTable tableDefaults() {
        return tableDefaults;
    }
    
    @JsonGetter("columnDefaults")
    public HoloConfigColumn columnDefaults() {
        return columnDefaults;
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
