package hu.webarticum.holodb.regex.ast;

public class LinebreakAstNode implements AstNode {

    @Override
    public int hashCode() {
        return LinebreakAstNode.class.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LinebreakAstNode);
    }

    @Override
    public String toString() {
        return "LB";
    }
    
}
