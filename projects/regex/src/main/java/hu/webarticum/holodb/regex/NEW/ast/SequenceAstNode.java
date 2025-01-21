package hu.webarticum.holodb.regex.NEW.ast;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class SequenceAstNode implements AstNode {

    private final ImmutableList<AstNode> nodes;
    
    private SequenceAstNode(ImmutableList<AstNode> nodes) {
        this.nodes = nodes;
    }

    public static SequenceAstNode of(ImmutableList<AstNode> nodes) {
        return new SequenceAstNode(nodes);
    }

    public ImmutableList<AstNode> nodes() {
        return nodes;
    }

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof SequenceAstNode)) {
            return false;
        }
        SequenceAstNode other = (SequenceAstNode) obj;
        return nodes.equals(other.nodes);
    }
    
    @Override
    public String toString() {
        return "seq" + nodes.toString();
    }
    
}
