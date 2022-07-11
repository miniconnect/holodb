package hu.webarticum.holodb.app.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HoloConfigColumn {
    
    public enum ColumnMode { DEFAULT, COUNTER, FIXED }
    

    private final String name;

    private final Class<?> type;

    private final ColumnMode mode;

    private final BigInteger nullCount;

    private final List<Object> values = new ArrayList<>();

    private final String valuesResource;

    private final List<BigInteger> valuesRange;

    
    public HoloConfigColumn(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("mode") ColumnMode mode,
            @JsonProperty("nullCount") BigInteger nullCount,
            @JsonProperty("values") List<Object> values,
            @JsonProperty("valuesResource") String valuesResource,
            @JsonProperty("valuesRange") List<BigInteger> valuesRange) {
        this.name = name;
        this.type = type;
        this.mode = mode == null ? ColumnMode.DEFAULT : mode;
        this.nullCount = nullCount == null ? BigInteger.ZERO : nullCount;
        if (values != null) {
            this.values.addAll(values);
        }
        this.valuesResource = valuesResource;
        this.valuesRange = valuesRange != null ? new ArrayList<>(valuesRange) : null;
    }
    

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public ColumnMode mode() {
        return mode;
    }

    public BigInteger nullCount() {
        return nullCount;
    }

    public List<Object> values() {
        return Collections.unmodifiableList(values);
    }

    public String valuesResource() {
        return valuesResource;
    }

    public List<BigInteger> valuesRange() {
        return valuesRange != null ? Collections.unmodifiableList(valuesRange) : null;
    }

}
