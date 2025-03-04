package hu.webarticum.holodb.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

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
    @JsonInclude(Include.NON_NULL)
    public HoloConfigSchema schemaDefaults() {
        return schemaDefaults;
    }

    @JsonGetter("tableDefaults")
    @JsonInclude(Include.NON_NULL)
    public HoloConfigTable tableDefaults() {
        return tableDefaults;
    }
    
    @JsonGetter("columnDefaults")
    @JsonInclude(Include.NON_NULL)
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
