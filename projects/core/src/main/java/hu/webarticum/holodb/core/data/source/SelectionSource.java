package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Optional;

import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class SelectionSource<T> implements Source<T> {

    private final Source<T> baseSource;
    
    private final Selection selection;
    
    
    public SelectionSource(Source<T> baseSource, Selection selection) {
        this.baseSource = baseSource;
        this.selection = selection;
    }
    
    
    @Override
    public Class<T> type() {
        return baseSource.type();
    }
    
    @Override
    public BigInteger size() {
        return selection.size();
    }

    @Override
    public T get(BigInteger index) {
        return baseSource.get(selection.at(index));
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
    }

}
