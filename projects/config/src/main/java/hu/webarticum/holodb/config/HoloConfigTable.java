package hu.webarticum.holodb.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class HoloConfigTable {
    
    private static final LargeInteger DEFAULT_SIZE = LargeInteger.of(50);
    

    private final String name;
    
    private final Boolean writeable;
    
    private final LargeInteger size;

    private final HoloConfigColumn columnDefaults;

    private final ImmutableList<HoloConfigColumn> columns;
    
    
    public HoloConfigTable(
            @JsonProperty("name") String name,
            @JsonProperty("writeable") Boolean writeable,
            @JsonProperty("size") LargeInteger size,
            @JsonProperty("columnDefaults") HoloConfigColumn columnDefaults,
            @JsonProperty("columns") ImmutableList<HoloConfigColumn> columns) {
        this.name = name;
        this.writeable = writeable;
        this.size = size;
        this.columnDefaults = columnDefaults;
        this.columns = columns;
    }

    public static HoloConfigTable empty() {
        return new HoloConfigTable(null, null, null, null, null);
    }
    
    public static HoloConfigTable createWithDefaults() {
        return new HoloConfigTable(
                null,
                false,
                DEFAULT_SIZE,
                null,
                ImmutableList.empty());
    }
    

    public HoloConfigTable merge(HoloConfigTable other) {
        if (other == null) {
            return this;
        }
        
        return new HoloConfigTable(
                mergeValue(name, other.name()),
                mergeValue(writeable, other.writeable()),
                mergeValue(size, other.size()),
                mergeValue(columnDefaults, other.columnDefaults()),
                mergeValue(columns, other.columns()));
    }
    
    private <T> T mergeValue(T fallbackValue, T mergeValue) {
        return (mergeValue != null) ? mergeValue : fallbackValue;
    }
    
    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("writeable")
    @JsonInclude(Include.NON_NULL)
    public Boolean writeable() {
        return writeable;
    }

    @JsonGetter("size")
    @JsonInclude(Include.NON_NULL)
    public LargeInteger size() {
        return size;
    }

    @JsonGetter("columnDefaults")
    @JsonInclude(Include.NON_NULL)
    public HoloConfigColumn columnDefaults() {
        return columnDefaults;
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
