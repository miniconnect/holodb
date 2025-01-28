package hu.webarticum.holodb.regex.ast;

public class CharacterConstantAstNode implements CharacterMatchAstNode {

    private final char value;
    
    private CharacterConstantAstNode(char value) {
        this.value = value;
    }

    public static CharacterConstantAstNode of(char value) {
        return new CharacterConstantAstNode(value);
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
        } else if (!(obj instanceof CharacterConstantAstNode)) {
            return false;
        }
        CharacterConstantAstNode other = (CharacterConstantAstNode) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }
    
}
