package hu.webarticum.holodb.regex.graph;

import java.util.ArrayList;

public class MutableNode {

    public Object value; // NOSONAR

    public ArrayList<MutableNode> children = new ArrayList<>(); // NOSONAR

    public MutableNode() {
        this(null);
    }
    
    public MutableNode(Object value, MutableNode... children) {
        this.value = value;
        for (MutableNode child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
    }
    
}
