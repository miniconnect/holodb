package hu.webarticum.holodb.core.util;

import java.util.Iterator;
import java.util.function.Function;

public class IteratorAdapter<T, U> implements Iterator<U> {
    
    private final Iterator<T> baseIterator;
    
    private final Function<T, U> mapFunction;
    

    public IteratorAdapter(Iterator<T> baseIterator, Function<T, U> mapFunction) {
        this.baseIterator = baseIterator;
        this.mapFunction = mapFunction;
    }
    
    
    @Override
    public boolean hasNext() {
        return baseIterator.hasNext();
    }

    @Override
    public U next() {
        return mapFunction.apply(baseIterator.next());
    }

}
