package hu.webarticum.holodb.regex.NEW.ast;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class CharacterClassAstNode implements CharacterMatchAstNode {

    private final boolean positive;

    private final ImmutableList<CharacterMatchAstNode> nodes;
    
    private CharacterClassAstNode(boolean positive, ImmutableList<CharacterMatchAstNode> nodes) {
        this.positive = positive;
        this.nodes = nodes;
    }

    public static CharacterClassAstNode of(boolean positive, ImmutableList<CharacterMatchAstNode> nodes) {
        return new CharacterClassAstNode(positive, nodes);
    }

    public ImmutableList<CharacterMatchAstNode> nodes() {
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
        } else if (!(obj instanceof CharacterClassAstNode)) {
            return false;
        }
        CharacterClassAstNode other = (CharacterClassAstNode) obj;
        return (
                positive == other.positive &&
                nodes.equals(other.nodes));
    }
    
    @Override
    public String toString() {
        return "class" + nodes.toString();
    }

}
