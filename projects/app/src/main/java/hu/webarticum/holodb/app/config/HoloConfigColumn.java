package hu.webarticum.holodb.app.config;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigColumn {
    
    public enum ColumnMode { DEFAULT, COUNTER, FIXED, ENUM }
    

    private final String name;

    private final Class<?> type;

    private final ColumnMode mode;

    private final BigInteger nullCount;

    private final List<Object> values = new ArrayList<>();

    private final String valuesResource;

    private final String valuesBundle;

    private final List<BigInteger> valuesRange;
    
    private final String valuesPattern;
    
    private final String valuesDynamicPattern;

    private final List<String> valuesForeignColumn;

    
    public HoloConfigColumn( // NOSONAR: many parameter is OK
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("mode") ColumnMode mode,
            @JsonProperty("nullCount") BigInteger nullCount,
            @JsonProperty("values") List<Object> values,
            @JsonProperty("valuesResource") String valuesResource,
            @JsonProperty("valuesBundle") String valuesBundle,
            @JsonProperty("valuesRange") List<BigInteger> valuesRange,
            @JsonProperty("valuesPattern") String valuesPattern,
            @JsonProperty("valuesDynamicPattern") String valuesDynamicPattern,
            @JsonProperty("valuesForeignColumn") List<String> valuesForeignColumn) {
        this.name = name;
        this.type = type;
        this.mode = mode == null ? ColumnMode.DEFAULT : mode;
        this.nullCount = nullCount == null ? BigInteger.ZERO : nullCount;
        if (values != null) {
            this.values.addAll(values);
        }
        this.valuesResource = valuesResource;
        this.valuesBundle = valuesBundle;
        this.valuesRange = valuesRange != null ? new ArrayList<>(valuesRange) : null;
        this.valuesPattern = valuesPattern;
        this.valuesDynamicPattern = valuesDynamicPattern;
        this.valuesForeignColumn = valuesForeignColumn;
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

    public String valuesBundle() {
        return valuesBundle;
    }

    public List<BigInteger> valuesRange() {
        return valuesRange != null ? Collections.unmodifiableList(valuesRange) : null;
    }

    public String valuesPattern() {
        return valuesPattern;
    }

    public String valuesDynamicPattern() {
        return valuesDynamicPattern;
    }

    public List<String> valuesForeignColumn() {
        return valuesForeignColumn != null ? Collections.unmodifiableList(valuesForeignColumn) : null;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("type", type)
                .add("mode", mode)
                .add("nullCount", nullCount)
                .add("values", values)
                .add("valuesResource", valuesResource)
                .add("valuesBundle", valuesBundle)
                .add("valuesRange", valuesRange)
                .add("valuesPattern", valuesPattern)
                .add("valuesDynamicPattern", valuesDynamicPattern)
                .add("valuesForeignColumn", valuesForeignColumn)
                .build();
    }
    
    @JsonValue
    public Map<String, Object> jsonValue() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("type", type);
        if (mode != ColumnMode.DEFAULT) {
            result.put("mode", mode);
        }
        if (!nullCount.equals(BigInteger.ZERO)) {
            result.put("nullCount", nullCount);
        }
        if (values != null) {
            result.put("values", values);
        }
        if (valuesResource != null) {
            result.put("valuesResource", valuesResource);
        }
        if (valuesBundle != null) {
            result.put("valuesBundle", valuesBundle);
        }
        if (valuesRange != null) {
            result.put("valuesRange", valuesRange);
        }
        if (valuesPattern != null) {
            result.put("valuesPattern", valuesPattern);
        }
        if (valuesDynamicPattern != null) {
            result.put("valuesDynamicPattern", valuesDynamicPattern);
        }
        if (valuesForeignColumn != null) {
            result.put("valuesForeignColumn", valuesForeignColumn);
        }
        return result;
    }

}
