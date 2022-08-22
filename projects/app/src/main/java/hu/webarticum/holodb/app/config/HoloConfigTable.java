package hu.webarticum.holodb.app.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigTable {

    private final String name;
    
    private final boolean writeable;
    
    private final BigInteger size;

    private final List<HoloConfigColumn> columns;
    
    
    public HoloConfigTable(
            @JsonProperty("name") String name,
            @JsonProperty("writeable") boolean writeable,
            @JsonProperty("size") BigInteger size,
            @JsonProperty("columns") List<HoloConfigColumn> columns) {
        this.name = name;
        this.writeable = writeable;
        this.size = size;
        this.columns = new ArrayList<>(columns);
    }
    

    public String name() {
        return name;
    }

    public boolean writeable() {
        return writeable;
    }
    
    public BigInteger size() {
        return size;
    }
    
    public List<HoloConfigColumn> columns() {
        return new ArrayList<>(columns);
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
