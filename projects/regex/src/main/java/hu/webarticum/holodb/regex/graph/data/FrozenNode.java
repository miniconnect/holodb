package hu.webarticum.holodb.regex.graph.data;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class FrozenNode {
    
    private final NodeData valueSet;
    
    private final ImmutableList<FrozenNode> children;
    
    private final LargeInteger length;
    
    private final LargeInteger itemLength;
    
    public FrozenNode(
            NodeData valueSet, ImmutableList<FrozenNode> children, LargeInteger length, LargeInteger itemLength) {
        this.valueSet = valueSet;
        this.children = children;
        this.length = length;
        this.itemLength = itemLength;
    }
    
    public NodeData data() {
        return valueSet;
    }

    public ImmutableList<FrozenNode> children() {
        return children;
    }

    public LargeInteger length() {
        return length;
    }

    public LargeInteger itemLength() {
        return itemLength;
    }
    
}
