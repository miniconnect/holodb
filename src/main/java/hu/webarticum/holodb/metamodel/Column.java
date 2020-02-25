package hu.webarticum.holodb.metamodel;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Column {

    private final Map<HintKey<?>, Object> hints;
    
    private final Table table;
    
    private final String name;
    
    private final Class<?> type;
    
    private final ValueGenerator<?> valueGenerator;
    
    private final boolean nullable;
    
    // TODO: requirements? (range, conditions etc.)
    
    
    private Column(ColumnBuilder builder, Table table) {
        hints = new HashMap<>(builder.hints);
        this.table = table;
        this.name = builder.name;
        this.type = builder.type;
        this.valueGenerator = builder.valueGenerator;
        this.nullable = builder.nullable;
    }
    

    @SuppressWarnings("unchecked")
    public <T> T getHint(HintKey<T> key) {
        return (T) hints.get(key);
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public ValueGenerator<?> getValueGenerator() {
        return valueGenerator;
    }

    public boolean isNullable() {
        return nullable;
    }
    
    @Override
    public String toString() {
        return name; // FIXME
    }
    
    
    public static ColumnBuilder builder() {
        return new ColumnBuilder();
    }
    
    
    public static final class ColumnBuilder implements Cloneable {

        private Map<HintKey<?>, Object> hints;

        private String name;
        
        private Class<?> type;
        
        private ValueGenerator<?> valueGenerator;
        
        private boolean nullable;
        
        
        private ColumnBuilder() {
            this.hints = new HashMap<>();
            this.name = "";
            this.type = Object.class; // FIXME
            this.valueGenerator = null;
            this.nullable = false;
        }

        private ColumnBuilder(ColumnBuilder base) {
            this.hints = new HashMap<>(base.hints);
            this.name = base.name;
            this.type = base.type;
            this.valueGenerator = base.valueGenerator;
            this.nullable = base.nullable;
        }
        

        public ColumnBuilder putHint(HintKey<BigInteger> key, long value) {
            return putHint(key, BigInteger.valueOf(value));
        }
        
        public <T> ColumnBuilder putHint(HintKey<T> key, T value) {
            if (value != null) {
                hints.put(key, value);
            } else {
                hints.remove(key);
            }
            return this;
        }
        
        public ColumnBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ColumnBuilder setType(Class<?> type) {
            this.type = type;
            this.valueGenerator = null;
            return this;
        }

        public <T> ColumnBuilder setType(Class<T> type, ValueGenerator<T> valueGenerator) {
            this.type = type;
            this.valueGenerator = valueGenerator;
            return this;
        }
        
        public ColumnBuilder setNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }
        
        @Override
        public ColumnBuilder clone() {
            return new ColumnBuilder(this);
        }
        
        public Column build(Table table) {
            return new Column(this, table);
        }
        
    }
    
}
