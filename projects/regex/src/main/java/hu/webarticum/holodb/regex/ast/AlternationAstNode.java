package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class AlternationAstNode implements AstNode {
    
    private final int startingPosition;

    private final ImmutableList<SequenceAstNode> branches;
    
    public AlternationAstNode(int startingPosition, ImmutableList<SequenceAstNode> branches) {
        this.startingPosition = startingPosition;
        this.branches = branches;
    }

    @Override
    public int startingPosition() {
        return startingPosition;
    }
    
    public ImmutableList<SequenceAstNode> branches() {
        return branches;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(startingPosition, branches);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AlternationAstNode)) {
            return false;
        }
        AlternationAstNode other = (AlternationAstNode) obj;
        return (
                startingPosition == other.startingPosition &&
                branches.equals(other.branches));
    }

    @Override
    public String toString() {
        return startingPosition + ":alt" + branches.toString();
    }
    
}
