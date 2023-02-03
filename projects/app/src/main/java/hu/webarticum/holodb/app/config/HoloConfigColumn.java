package hu.webarticum.holodb.app.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.holodb.spi.config.SourceFactory;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.util.ToStringBuilder;

public class HoloConfigColumn {
    
    public enum ColumnMode {
        
        DEFAULT, COUNTER, FIXED, ENUM
    
    }
    
    public enum ShuffleQuality {
        
        NOOP, VERY_LOW, LOW, MEDIUM, HIGH, VERY_HIGH
    
    }
    
    
    private final String name;

    private final Class<?> type;

    private final ColumnMode mode;

    private final LargeInteger nullCount;

    private final ImmutableList<Object> values;

    private final String valuesResource;

    private final String valuesBundle;

    private final ImmutableList<LargeInteger> valuesRange;
    
    private final String valuesPattern;
    
    private final String valuesDynamicPattern;

    private final ImmutableList<String> valuesForeignColumn;
    
    private final ShuffleQuality shuffleQuality;
    
    private final Class<? extends SourceFactory> sourceFactory;
    
    private final Object sourceFactoryData;
    
    private final Object defaultValue;

    
    public HoloConfigColumn( // NOSONAR: many parameter is OK
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("mode") ColumnMode mode,
            @JsonProperty("nullCount") LargeInteger nullCount,
            @JsonProperty("values") ImmutableList<Object> values,
            @JsonProperty("valuesResource") String valuesResource,
            @JsonProperty("valuesBundle") String valuesBundle,
            @JsonProperty("valuesRange") ImmutableList<LargeInteger> valuesRange,
            @JsonProperty("valuesPattern") String valuesPattern,
            @JsonProperty("valuesDynamicPattern") String valuesDynamicPattern,
            @JsonProperty("valuesForeignColumn") ImmutableList<String> valuesForeignColumn,
            @JsonProperty("shuffleQuality") ShuffleQuality shuffleQuality,
            @JsonProperty("sourceFactory") Class<? extends SourceFactory> sourceFactory,
            @JsonProperty("sourceFactoryData") Object sourceFactoryData,
            @JsonProperty("defaultValue") Object defaultValue) {
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.nullCount = nullCount;
        this.values = values;
        this.valuesResource = valuesResource;
        this.valuesBundle = valuesBundle;
        this.valuesRange = valuesRange;
        this.valuesPattern = valuesPattern;
        this.valuesDynamicPattern = valuesDynamicPattern;
        this.valuesForeignColumn = valuesForeignColumn;
        this.shuffleQuality = shuffleQuality;
        this.sourceFactory = sourceFactory;
        this.sourceFactoryData = sourceFactoryData;
        this.defaultValue = defaultValue;
    }

    public static HoloConfigColumn empty() {
        return new HoloConfigColumn(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    public static HoloConfigColumn createWithDefaults() {
        return new HoloConfigColumn(
                null,
                null,
                ColumnMode.DEFAULT, // FIXME null == NON NULL?
                LargeInteger.ZERO,
                ImmutableList.empty(), // FIXME: should null be supported?
                null,
                null,
                null,
                null,
                null,
                null,
                ShuffleQuality.MEDIUM,
                null,
                null,
                null);
    }
    

    public HoloConfigColumn merge(HoloConfigColumn other) {
        if (other == null) {
            return this;
        }
        
        return new HoloConfigColumn(
                mergeValue(name, other.name()),
                mergeValue(type, other.type()),
                mergeValue(mode, other.mode()),
                mergeValue(nullCount, other.nullCount()),
                mergeValue(values, other.values()),
                mergeValue(valuesResource, other.valuesResource()),
                mergeValue(valuesBundle, other.valuesBundle()),
                mergeValue(valuesRange, other.valuesRange()),
                mergeValue(valuesPattern, other.valuesPattern()),
                mergeValue(valuesDynamicPattern, other.valuesDynamicPattern()),
                mergeValue(valuesForeignColumn, other.valuesForeignColumn()),
                mergeValue(shuffleQuality, other.shuffleQuality()),
                mergeValue(sourceFactory, other.sourceFactory()),
                mergeValue(sourceFactoryData, other.sourceFactoryData()),
                mergeValue(defaultValue, other.defaultValue()));
    }
    
    private <T> T mergeValue(T fallbackValue, T mergeValue) {
        return (mergeValue != null) ? mergeValue : fallbackValue;
    }
    
    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("type")
    public Class<?> type() {
        return type;
    }

    @JsonGetter("mode")
    @JsonInclude(Include.NON_NULL)
    public ColumnMode mode() {
        return mode;
    }
    
    @JsonGetter("nullCount")
    @JsonInclude(Include.NON_NULL)
    public LargeInteger nullCount() {
        return nullCount;
    }

    // FIXME: handle empty/null
    @JsonGetter("values")
    @JsonInclude(Include.NON_NULL)
    public ImmutableList<Object> values() {
        return values;
    }

    @JsonGetter("valuesResource")
    @JsonInclude(Include.NON_NULL)
    public String valuesResource() {
        return valuesResource;
    }

    @JsonGetter("valuesBundle")
    @JsonInclude(Include.NON_NULL)
    public String valuesBundle() {
        return valuesBundle;
    }

    @JsonGetter("valuesRange")
    @JsonInclude(Include.NON_NULL)
    public ImmutableList<LargeInteger> valuesRange() {
        return valuesRange;
    }

    @JsonGetter("valuesPattern")
    @JsonInclude(Include.NON_NULL)
    public String valuesPattern() {
        return valuesPattern;
    }

    @JsonGetter("valuesDynamicPattern")
    @JsonInclude(Include.NON_NULL)
    public String valuesDynamicPattern() {
        return valuesDynamicPattern;
    }

    @JsonGetter("valuesForeignColumn")
    @JsonInclude(Include.NON_NULL)
    public ImmutableList<String> valuesForeignColumn() {
        return valuesForeignColumn;
    }

    @JsonGetter("shuffleQuality")
    @JsonInclude(Include.NON_NULL)
    public ShuffleQuality shuffleQuality() {
        return shuffleQuality;
    }

    @JsonGetter("sourceFactory")
    @JsonInclude(Include.NON_NULL)
    public Class<? extends SourceFactory> sourceFactory() {
        return sourceFactory;
    }

    @JsonGetter("sourceFactoryData")
    @JsonInclude(Include.NON_NULL)
    public Object sourceFactoryData() {
        return sourceFactoryData;
    }

    @JsonGetter("defaultValue")
    @JsonInclude(Include.NON_NULL)
    public Object defaultValue() {
        return defaultValue;
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
                .add("shuffleQuality", shuffleQuality)
                .add("sourceFactory", sourceFactory)
                .add("sourceFactoryData", sourceFactoryData)
                .add("defaultValue", defaultValue)
                .build();
    }

}
