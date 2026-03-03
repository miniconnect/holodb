package hu.webarticum.holodb.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class HoloConfigSchema {

    private final String name;

    private final HoloConfigTable tableDefaults;

    private final HoloConfigColumn columnDefaults;

    private final ImmutableList<HoloConfigTable> tables;


    public HoloConfigSchema(
            @JsonProperty("name") String name,
            @JsonProperty("tableDefaults") HoloConfigTable tableDefaults,
            @JsonProperty("columnDefaults") HoloConfigColumn columnDefaults,
            @JsonProperty("tables") ImmutableList<HoloConfigTable> tables) {
        this.name = name;
        this.tableDefaults = tableDefaults;
        this.columnDefaults = columnDefaults;
        this.tables = tables;
    }

    public static HoloConfigSchema empty() {
        return new HoloConfigSchema(null, null, null, null);
    }

    public static HoloConfigSchema createWithDefaults() {
        return new HoloConfigSchema(null, null, null, ImmutableList.empty());
    }


    public HoloConfigSchema merge(HoloConfigSchema other) {
        if (other == null) {
            return this;
        }

        return new HoloConfigSchema(
                mergeValue(name, other.name()),
                mergeValue(tableDefaults, other.tableDefaults()),
                mergeValue(columnDefaults, other.columnDefaults()),
                mergeValue(tables, other.tables()));
    }

    private <T> T mergeValue(T fallbackValue, T mergeValue) {
        return (mergeValue != null) ? mergeValue : fallbackValue;
    }

    @JsonGetter("name")
    public String name() {
        return name;
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
