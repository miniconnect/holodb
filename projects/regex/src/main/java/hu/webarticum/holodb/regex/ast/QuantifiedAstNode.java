package hu.webarticum.holodb.regex.ast;

public class QuantifiedAstNode implements AstNode {

    private final AstNode node;

    private final int minOccurences;

    private final int maxOccurences;
    
    public QuantifiedAstNode(AstNode node, int minOccurences, int maxOccurences) {
        this.node = node;
        this.minOccurences = minOccurences;
        this.maxOccurences = maxOccurences;
    }

    public AstNode node() {
        return node;
    }

    public int minOccurences() {
        return minOccurences;
    }

    public int maxOccurences() {
        return maxOccurences;
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof QuantifiedAstNode)) {
            return false;
        } else {
            return node.equals(((QuantifiedAstNode) obj).node);
        }
    }

    @Override
    public String toString() {
        return "quant{node: " + node + ", min:" + minOccurences + ", max: " + maxOccurences + "}";
    }
    
}
