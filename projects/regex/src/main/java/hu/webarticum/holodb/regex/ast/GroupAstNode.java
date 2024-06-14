package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

public class GroupAstNode implements AstNode {
    
    public enum Kind {
        CAPTURING, NAMED, NON_CAPTURING
    }
    
    private final int startingPosition;

    private final AlternationAstNode alternation;

    private final Kind kind;
    
    private final String name;
    
    public GroupAstNode(int startingPosition, AlternationAstNode alternation, Kind kind, String name) {
        this.startingPosition = startingPosition;
        this.alternation = alternation;
        this.kind = kind;
        this.name = name;
    }

    @Override
    public int startingPosition() {
        return startingPosition;
    }
    
    public AlternationAstNode alternation() {
        return alternation;
    }

    public Kind kind() {
        return kind;
    }

    public String name() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startingPosition, alternation, kind, name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof GroupAstNode)) {
            return false;
        }
        GroupAstNode other = (GroupAstNode) obj;
        return (
                startingPosition == other.startingPosition &&
                alternation.equals(other.alternation) &&
                kind.equals(other.kind) &&
                name.equals(other.name));
    }

    @Override
    public String toString() {
        return startingPosition + ":group{alternation: " + alternation + ", kind:" + kind + ", name: " + name + "}";
    }
    
}
