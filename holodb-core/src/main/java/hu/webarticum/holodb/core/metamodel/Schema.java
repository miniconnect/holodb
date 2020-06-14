package hu.webarticum.holodb.core.metamodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

// FIXME omit get/set prefixes?

public class Schema {

    private final Map<HintKey<?>, Object> hints;
    
    private final SortedMap<String, Table> tables;

    private final SortedMap<String, Relationship> relationships;
    
    
    private Schema(SchemaBuilder builder) {
        hints = new HashMap<>(builder.hints);
        tables = new TreeMap<>();
        for (Table.TableBuilder tableBuilder : builder.tableBuilders) {
            Table table = tableBuilder.build(this);
            tables.put(table.getName(), table);
        }
        relationships = new TreeMap<>();
        for (Relationship.RelationshipBuilder relationshipBuilder : builder.relationshipBuilders) {
            Relationship relationship = relationshipBuilder.build(this);
            relationships.put(relationship.getName(), relationship);
        }
    }
    
    
    @SuppressWarnings("unchecked")
    public <T> T getHint(HintKey<T> key) {
        return (T) hints.get(key);
    }
    
    public Table getTable(String name) {
        return tables.get(name);
    }

    public List<Table> getTables() {
        return new ArrayList<>(tables.values());
    }

    public Relationship getRelationship(String name) {
        return relationships.get(name);
    }

    public List<Relationship> getRelationships() {
        return new ArrayList<>(relationships.values());
    }

    @Override
    public String toString() {
        return "Schema-xxx"; // FIXME
    }
    
    
    public static SchemaBuilder builder() {
        return new SchemaBuilder();
    }
    
    
    public static final class SchemaBuilder implements Cloneable {

        private Map<HintKey<?>, Object> hints;

        private List<Table.TableBuilder> tableBuilders;

        private List<Relationship.RelationshipBuilder> relationshipBuilders;
        

        private SchemaBuilder() {
            this.hints = new HashMap<>();
            this.tableBuilders = new ArrayList<>();
            this.relationshipBuilders = new ArrayList<>();
        }

        private SchemaBuilder(SchemaBuilder base) {
            this.hints = new HashMap<>(base.hints);
            this.tableBuilders = new ArrayList<>(base.tableBuilders.size());
            for (Table.TableBuilder tableBuilder : base.tableBuilders) {
                this.tableBuilders.add(tableBuilder);
            }
            this.relationshipBuilders = new ArrayList<>(base.relationshipBuilders.size());
            for (Relationship.RelationshipBuilder relationshipBuilder : base.relationshipBuilders) {
                this.relationshipBuilders.add(relationshipBuilder);
            }
        }
        

        public SchemaBuilder putHint(HintKey<BigInteger> key, long value) {
            return putHint(key, BigInteger.valueOf(value));
        }
        
        public <T> SchemaBuilder putHint(HintKey<T> key, T value) {
            if (value != null) {
                hints.put(key, value);
            } else {
                hints.remove(key);
            }
            return this;
        }
        
        public SchemaBuilder addTable(Table.TableBuilder tableBuilder) {
            tableBuilders.add(tableBuilder.clone());
            return this;
        }

        public SchemaBuilder addRelationship(Relationship.RelationshipBuilder relationshipBuilder) {
            relationshipBuilders.add(relationshipBuilder.clone());
            return this;
        }
        
        @Override
        protected SchemaBuilder clone() {
            return new SchemaBuilder(this);
        }
        
        public Schema build() {
            return new Schema(this);
        }
        
    }
    
}
