package hu.webarticum.holodb.regex.ast;

public class BuiltinCharacterClassAstNode implements AstNode {

    public enum Kind {
        ANY,
        WORD,
        NON_WORD,
        DIGIT,
        NON_DIGIT,
        WHITESPACE,
        NON_WHITESPACE,
        HORIZONTAL_WHITESPACE,
        NON_HORIZONTAL_WHITESPACE,
        VERTICAL_WHITESPACE,
        NON_VERTICAL_WHITESPACE,
    }
    
    private final Kind kind;
    
    public BuiltinCharacterClassAstNode(Kind kind) {
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return kind.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof BuiltinCharacterClassAstNode)) {
            return false;
        }
        BuiltinCharacterClassAstNode other = (BuiltinCharacterClassAstNode) obj;
        return kind == other.kind;
    }

    @Override
    public String toString() {
        return kind.toString();
    }
    
}
