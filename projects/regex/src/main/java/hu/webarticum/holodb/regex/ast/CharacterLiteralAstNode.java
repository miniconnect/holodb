package hu.webarticum.holodb.regex.ast;

public class CharacterLiteralAstNode implements AstNode {

    private final char value;
    
    public CharacterLiteralAstNode(char value) {
        this.value = value;
    }

    public char value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof CharacterLiteralAstNode)) {
            return false;
        }
        CharacterLiteralAstNode other = (CharacterLiteralAstNode) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
    
}
