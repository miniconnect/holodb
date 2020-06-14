package hu.webarticum.holodb.core.query.model;

import java.util.Objects;

public final class TextLiteral implements Literal {

    private final String text;
    
    
    public TextLiteral(String text) {
        this.text = Objects.requireNonNull(text);
    }
    
    
    public String getText() {
        return text;
    }
    
    @Override
    public Type getType() {
        return Type.TEXT;
    }
    
    @Override
    public int hashCode() {
        return text.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextLiteral)) {
            return false;
        }
        
        return text.equals(((TextLiteral) obj).text);
    }
    
}
