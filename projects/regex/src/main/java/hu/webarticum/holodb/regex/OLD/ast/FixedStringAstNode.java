package hu.webarticum.holodb.regex.OLD.ast;

public class FixedStringAstNode implements AstNode {

    private final String value;
    
    private FixedStringAstNode(String value) {
        this.value = value;
    }

    public static FixedStringAstNode of(String value) {
        return new FixedStringAstNode(value);
    }

    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof FixedStringAstNode)) {
            return false;
        }
        FixedStringAstNode other = (FixedStringAstNode) obj;
        return value.equals(other.value);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
    
}
