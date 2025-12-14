package hu.webarticum.holodb.regex.ast;

public class NamedBackreferenceAstNode implements AstNode {

    private final String name;

    private NamedBackreferenceAstNode(String name) {
        this.name = name;
    }

    public static NamedBackreferenceAstNode of(String name) {
        return new NamedBackreferenceAstNode(name);
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof NamedBackreferenceAstNode)) {
            return false;
        }
        NamedBackreferenceAstNode other = (NamedBackreferenceAstNode) obj;
        return name.equals(other.name);
    }

    @Override
    public String toString() {
        return "back:'" + name + "'";
    }

}
