package hu.webarticum.holodb.regex.OLD.ast;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class AlternationAstNode implements AstNode {
    
    private final ImmutableList<SequenceAstNode> branches;
    
    private AlternationAstNode(ImmutableList<SequenceAstNode> branches) {
        this.branches = branches;
    }
    
    public static AlternationAstNode of(ImmutableList<SequenceAstNode> branches) {
        return new AlternationAstNode(branches);
    }

    public ImmutableList<SequenceAstNode> branches() {
        return branches;
    }
    
    @Override
    public int hashCode() {
        return branches.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AlternationAstNode)) {
            return false;
        }
        AlternationAstNode other = (AlternationAstNode) obj;
        return branches.equals(other.branches);
    }

    @Override
    public String toString() {
        return "alt" + branches.toString();
    }
    
}
