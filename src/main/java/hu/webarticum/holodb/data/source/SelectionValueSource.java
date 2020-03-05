package hu.webarticum.holodb.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.data.selection.Selection;

// TODO: [Indexed|Sorted]SelectionValueSource?

public class SelectionValueSource<T> implements ValueSource<T> {

    private final ValueSource<T> baseSource;
    
    private final Selection selection;
    
    
    public SelectionValueSource(ValueSource<T> baseSource, Selection selection) {
        this.baseSource = baseSource;
        this.selection = selection;
    }
    
    
    @Override
    public BigInteger size() {
        return selection.getCount();
    }

    @Override
    public T at(BigInteger index) {
        return baseSource.at(selection.at(index));
    }

}
