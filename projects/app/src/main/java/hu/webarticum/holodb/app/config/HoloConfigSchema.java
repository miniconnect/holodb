package hu.webarticum.holodb.app.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigSchema {

    private final String name;

    private final ImmutableList<HoloConfigTable> tables;
    
    
    public HoloConfigSchema(
            @JsonProperty("name") String name,
            @JsonProperty("tables") ImmutableList<HoloConfigTable> tables) {
        this.name = Objects.requireNonNull(name, "Schema name must be specified");
        this.tables = tables != null ? tables : ImmutableList.empty();
    }
    

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("tables")
    public ImmutableList<HoloConfigTable> tables() {
        return tables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("tables", tables)
                .build();
    }
    
}
