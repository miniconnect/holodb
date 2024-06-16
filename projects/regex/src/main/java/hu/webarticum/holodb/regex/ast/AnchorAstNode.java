package hu.webarticum.holodb.regex.ast;

public class AnchorAstNode implements AstNode {

    public enum Kind {
        WORD_BOUNDARY,
        NON_WORD_BOUNDARY,
        BEGIN_OF_LINE,
        END_OF_LINE,
        BEGIN_OF_INPUT,
        END_OF_INPUT,
        END_OF_INPUT_ALLOW_NEWLINE,
        END_OF_PREVIOUS_MATCH,
    }
    
    private final Kind kind;
    
    public AnchorAstNode(Kind kind) {
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
        } else if (!(obj instanceof AnchorAstNode)) {
            return false;
        }
        AnchorAstNode other = (AnchorAstNode) obj;
        return kind == other.kind;
    }

    @Override
    public String toString() {
        return kind.toString();
    }
    
}
