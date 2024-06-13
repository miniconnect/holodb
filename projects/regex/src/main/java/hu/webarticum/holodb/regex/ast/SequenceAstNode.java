package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class SequenceAstNode implements AstNode {

    private final int startingPosition;

    private final ImmutableList<AstNode> nodes;
    
    public SequenceAstNode(int startingPosition, ImmutableList<AstNode> nodes) {
        this.startingPosition = startingPosition;
        this.nodes = nodes;
    }

    @Override
    public int startingPosition() {
        return startingPosition;
    }
    
    public ImmutableList<AstNode> nodes() {
        return nodes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingPosition, nodes);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SequenceAstNode)) {
            return false;
        }
        SequenceAstNode other = (SequenceAstNode) obj;
        return (
                startingPosition == other.startingPosition &&
                nodes.equals(other.nodes));
    }
    
    @Override
    public String toString() {
        return startingPosition + ":seq" + nodes.toString();
    }
    
}
