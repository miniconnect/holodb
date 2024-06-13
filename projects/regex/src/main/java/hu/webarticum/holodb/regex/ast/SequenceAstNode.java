package hu.webarticum.holodb.regex.ast;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class SequenceAstNode implements AstNode {

    private final ImmutableList<AstNode> nodes;
    
    public SequenceAstNode(ImmutableList<AstNode> nodes) {
        this.nodes = nodes;
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
        } else {
            return nodes.equals(((SequenceAstNode) obj).nodes);
        }
    }
    
    @Override
    public String toString() {
        return "seq" + nodes.toString();
    }
    
}
