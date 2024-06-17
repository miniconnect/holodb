package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

public class GroupAstNode implements AstNode {
    
    public enum Kind {
        CAPTURING, NAMED, NON_CAPTURING
    }
    
    private final AlternationAstNode alternation;

    private final Kind kind;
    
    private final String name;
    
    private GroupAstNode(AlternationAstNode alternation, Kind kind, String name) {
        this.alternation = alternation;
        this.kind = kind;
        this.name = name;
    }

    public static GroupAstNode of(AlternationAstNode alternation, Kind kind, String name) {
        return new GroupAstNode(alternation, kind, name);
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
        return Objects.hash(alternation, kind, name);
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
                alternation.equals(other.alternation) &&
                kind.equals(other.kind) &&
                name.equals(other.name));
    }

    @Override
    public String toString() {
        return "group{alternation: " + alternation + ", kind:" + kind + ", name: " + name + "}";
    }
    
}
