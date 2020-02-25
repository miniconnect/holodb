package hu.webarticum.holodb.metamodel;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Relationship {

    private final Map<HintKey<?>, Object> hints;
    
    private final Schema schema;
    
    private final String name;
    
    
    private Relationship(RelationshipBuilder builder, Schema schema) {
        hints = new HashMap<>(builder.hints);
        this.schema = schema;
        this.name = builder.name;
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

    @Override
    public String toString() {
        return "Relationship-xxx";
    }
    
    
    public static RelationshipBuilder builder() {
        return new RelationshipBuilder();
    }
    
    
    public static final class RelationshipBuilder implements Cloneable {

        private Map<HintKey<?>, Object> hints;

        private String name;
        

        private RelationshipBuilder() {
            this.hints = new HashMap<>();
            this.name = "";
        }        

        private RelationshipBuilder(RelationshipBuilder base) {
            this.hints = new HashMap<>(base.hints);
            this.name = base.name;
        }
        

        public void putHint(HintKey<BigInteger> key, long value) {
            putHint(key, BigInteger.valueOf(value));
        }
        
        public <T> void putHint(HintKey<T> key, T value) {
            if (value != null) {
                hints.put(key, value);
            } else {
                hints.remove(key);
            }
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        protected RelationshipBuilder clone() {
            return new RelationshipBuilder(this);
        }
        
        public Relationship build(Schema schema) {
            return new Relationship(this, schema);
        }
        
    }
    
}
