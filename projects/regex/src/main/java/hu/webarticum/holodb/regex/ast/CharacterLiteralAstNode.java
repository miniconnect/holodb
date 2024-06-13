package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

public class CharacterLiteralAstNode implements AstNode {

    private final int startingPosition;

    private final char value;
    
    public CharacterLiteralAstNode(int startingPosition, char value) {
        this.startingPosition = startingPosition;
        this.value = value;
    }

    @Override
    public int startingPosition() {
        return startingPosition;
    }
    
    public char value() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingPosition, value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof CharacterLiteralAstNode)) {
            return false;
        }
        CharacterLiteralAstNode other = (CharacterLiteralAstNode) obj;
        return (
                startingPosition == other.startingPosition &&
                value == other.value);
    }

    @Override
    public String toString() {
        return startingPosition + ":'" + value + "'";
    }
    
}
