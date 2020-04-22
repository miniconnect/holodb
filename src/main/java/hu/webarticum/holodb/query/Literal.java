package hu.webarticum.holodb.query;

public interface Literal extends Expression {

    public enum Type {
        // TODO
        TEXT
    }
    
    
    public Type getType();
    
}
