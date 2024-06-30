package hu.webarticum.holodb.regex.ast;

import java.util.Objects;

public class QuantifiedAstNode implements AstNode {
    
    public static final int NO_UPPER_LIMIT = Integer.MAX_VALUE;

    private final AstNode node;

    private final int minOccurrences;

    private final int maxOccurrences;
    
    private QuantifiedAstNode(AstNode node, int minOccurrences, int maxOccurrences) {
        this.node = node;
        this.minOccurrences = minOccurrences;
        this.maxOccurrences = maxOccurrences;
    }

    public static QuantifiedAstNode of(AstNode node, int minOccurrences, int maxOccurrences) {
        return new QuantifiedAstNode(node, minOccurrences, maxOccurrences);
    }

    public AstNode node() {
        return node;
    }

    public int minOccurrences() {
        return minOccurrences;
    }

    public int maxOccurrences() {
        return maxOccurrences;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, minOccurrences, maxOccurrences);
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
                minOccurrences == other.minOccurrences &&
                maxOccurrences == other.maxOccurrences);
    }

    @Override
    public String toString() {
        return "quant{node: " + node + ", min:" + minOccurrences + ", max: " + maxOccurrences + "}";
    }
    
}
