package hu.webarticum.holodb.regex.ast;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class AlternationAstNode implements AstNode {

    private final ImmutableList<SequenceAstNode> branches;
    
    public AlternationAstNode(ImmutableList<SequenceAstNode> branches) {
        this.branches = branches;
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
        } else {
            return branches.equals(((AlternationAstNode) obj).branches);
        }
    }

    @Override
    public String toString() {
        return "alt" + branches.toString();
    }
    
}
