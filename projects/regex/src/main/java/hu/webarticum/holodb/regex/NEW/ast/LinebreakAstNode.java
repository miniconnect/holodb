package hu.webarticum.holodb.regex.NEW.ast;

public class LinebreakAstNode implements AstNode {

    private static final LinebreakAstNode instance = new LinebreakAstNode();
    
    private LinebreakAstNode() {
        // singleton
    }
    
    public static LinebreakAstNode instance() {
        return instance;
    }
    
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
