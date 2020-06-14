package hu.webarticum.holodb.core.metamodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {

    private final Map<HintKey<?>, Object> hints;
    
    private final Schema schema;
    
    private final String name;
    
    private final List<Column> columns;
    
    
    private Table(TableBuilder builder, Schema schema) {
        hints = new HashMap<>(builder.hints);
        this.schema = schema;
        this.name = builder.name;
        this.columns = new ArrayList<>(builder.columnBuilders.size());
        for (Column.ColumnBuilder columnBuilder : builder.columnBuilders) {
            this.columns.add(columnBuilder.build(this));
        }
    }
    

    @SuppressWarnings("unchecked")
    public <T> T getHint(HintKey<T> key) {
        return (T) hints.get(key);
    }

    public Schema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }
    
    public List<Column> getColumns() {
        return new ArrayList<>(columns);
    }

    @Override
    public String toString() {
        return name; // FIXME
    }
    
    
    public static TableBuilder builder() {
        return new TableBuilder();
    }
    
    
    public static final class TableBuilder implements Cloneable {

        private Map<HintKey<?>, Object> hints;

        private String name;
        
        private List<Column.ColumnBuilder> columnBuilders;
        

        private TableBuilder() {
            this.hints = new HashMap<>();
            this.name = "";
            this.columnBuilders = new ArrayList<>();
        }

        private TableBuilder(TableBuilder base) {
            this.hints = new HashMap<>(base.hints);
            this.name = base.name;
            this.columnBuilders = new ArrayList<>(base.columnBuilders.size());
            for (Column.ColumnBuilder columnBuilder : base.columnBuilders) {
                this.columnBuilders.add(columnBuilder.clone());
            }
        }
        

        public TableBuilder putHint(HintKey<BigInteger> key, long value) {
            return putHint(key, BigInteger.valueOf(value));
        }
        
        public <T> TableBuilder putHint(HintKey<T> key, T value) {
            if (value != null) {
                hints.put(key, value);
            } else {
                hints.remove(key);
            }
            return this;
        }
        
        public TableBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public TableBuilder addColumn(Column.ColumnBuilder columnBuilder) {
            this.columnBuilders.add(columnBuilder.clone());
            return this;
        }
        
        @Override
        protected TableBuilder clone() {
            return new TableBuilder(this);
        }
        
        public Table build(Schema schema) {
            return new Table(this, schema);
        }
        
    }
    
}
