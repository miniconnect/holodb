package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

public class QuantifiedAstNode implements AstNode {
    
    public static final int NO_UPPER_LIMIT = Integer.MAX_VALUE;

    private final AstNode node;

    private final int minOccurences;

    private final int maxOccurences;
    
    private QuantifiedAstNode(AstNode node, int minOccurences, int maxOccurences) {
        this.node = node;
        this.minOccurences = minOccurences;
        this.maxOccurences = maxOccurences;
    }

    public static QuantifiedAstNode of(AstNode node, int minOccurences, int maxOccurences) {
        return new QuantifiedAstNode(node, minOccurences, maxOccurences);
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
        return Objects.hash(node, minOccurences, maxOccurences);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof QuantifiedAstNode)) {
            return false;
        }
        QuantifiedAstNode other = (QuantifiedAstNode) obj;
        return (
                node.equals(other.node) &&
                minOccurences == other.minOccurences &&
                maxOccurences == other.maxOccurences);
    }

    @Override
    public String toString() {
        return "quant{node: " + node + ", min:" + minOccurences + ", max: " + maxOccurences + "}";
    }
    
}
