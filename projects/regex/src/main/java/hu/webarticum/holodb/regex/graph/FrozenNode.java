package hu.webarticum.holodb.regex.graph;

import java.util.Comparator;

import hu.webarticum.holodb.regex.ast.extract.ExtractableValueSet;
import hu.webarticum.holodb.regex.ast.extract.ExtractableNode;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class FrozenNode implements ExtractableNode<FrozenNode> {
    
    private final ExtractableValueSet valueSet;
    
    private final ImmutableList<FrozenNode> children;
    
    private final LargeInteger length;
    
    private final LargeInteger itemLength;
    
    public FrozenNode(
            ExtractableValueSet valueSet, ImmutableList<FrozenNode> children, LargeInteger length, LargeInteger itemLength) {
        this.valueSet = valueSet;
        this.children = children;
        this.length = length;
        this.itemLength = itemLength;
    }
    
    @Override
    public ExtractableValueSet data() {
        return valueSet;
    }

    @Override
    public ImmutableList<FrozenNode> children() {
        return children;
    }

    @Override
    public LargeInteger length() {
        return length;
    }

    @Override
    public LargeInteger subLength() {
        return itemLength;
    }
    
    @Override
    public Comparator<Object> valueComparator() {
        return GraphValueComparator.instance();
    }
    
}
