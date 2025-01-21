package hu.webarticum.holodb.regex.NEW.ast;

import java.util.Objects;

public class RangeAstNode implements CharacterMatchAstNode {
    
    private final char low;
    
    private final char high;
    
    private RangeAstNode(char low, char high) {
        this.low = low;
        this.high = high;
    }

    public static RangeAstNode of(char low, char high) {
        return new RangeAstNode(low, high);
    }

    public char low() {
        return low;
    }

    public char high() {
        return high;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof RangeAstNode)) {
            return false;
        }
        RangeAstNode other = (RangeAstNode) obj;
        return (
                low == other.low &&
                high == other.high);
    }
    
    @Override
    public String toString() {
        return "range:'" + low + "'..'" + high + "'";
    }
    
}