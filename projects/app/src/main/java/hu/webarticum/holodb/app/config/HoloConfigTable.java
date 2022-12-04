package hu.webarticum.holodb.app.config;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigTable {

    private final String name;
    
    private final boolean writeable;
    
    private final LargeInteger size;

    private final ImmutableList<HoloConfigColumn> columns;
    
    
    public HoloConfigTable(
            @JsonProperty("name") String name,
            @JsonProperty("writeable") boolean writeable,
            @JsonProperty("size") LargeInteger size,
            @JsonProperty("columns") ImmutableList<HoloConfigColumn> columns) {
        this.name = Objects.requireNonNull(name, "Table name must be specified");
        this.writeable = writeable;
        this.size = Objects.requireNonNull(size, "Table size must be specified");
        this.columns = columns != null ? columns : ImmutableList.empty();
    }
    

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("writeable")
    public boolean writeable() {
        return writeable;
    }

    @JsonGetter("size")
    public LargeInteger size() {
        return size;
    }

    @JsonGetter("columns")
    public ImmutableList<HoloConfigColumn> columns() {
        return columns;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("writeable", writeable)
                .add("size", size)
                .add("columns", columns)
                .build();
    }
    
}
