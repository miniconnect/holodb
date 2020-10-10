package hu.webarticum.holodb.core.query.model;

public interface Literal extends Expression {

    public enum Type {
        // TODO
        TEXT
    }
    
    
    public Type getType();
    
    // FIXME: Object getValue() ?
    
}
