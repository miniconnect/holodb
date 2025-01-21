package hu.webarticum.holodb.regex.OLD.ast;

public class BackreferenceAstNode implements AstNode {

    private final int number;
    
    private BackreferenceAstNode(int number) {
        this.number = number;
    }

    public static BackreferenceAstNode of(int number) {
        return new BackreferenceAstNode(number);
    }

    public int number() {
        return number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BackreferenceAstNode)) {
            return false;
        }
        BackreferenceAstNode other = (BackreferenceAstNode) obj;
        return number == other.number;
    }

    @Override
    public String toString() {
        return "back:" + number;
    }
    
}
