package hu.webarticum.holodb.regex.ast.extract;

import java.util.Comparator;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface ExtractableNode<T extends ExtractableNode<T>> {

    public ExtractableValueSet data();

    public ImmutableList<T> children();

    public LargeInteger length();

    public LargeInteger subLength();
    
    public Comparator<Object> valueComparator();
    
}
