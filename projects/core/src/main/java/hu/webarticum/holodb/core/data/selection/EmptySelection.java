package hu.webarticum.holodb.core.data.selection;

import java.util.Collections;
import java.util.Iterator;

import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ReversibleIterable;

public final class EmptySelection implements Selection {

    private static final EmptySelection INSTANCE = new EmptySelection();
    
    
    private EmptySelection() {
        // use instance()
    }
    
    
    public static EmptySelection instance() {
        return INSTANCE;
    }


    @Override
    public Iterator<LargeInteger> iterator() {
        return Collections.emptyIterator();
    }


    @Override
    public LargeInteger size() {
        return LargeInteger.ZERO;
    }


    @Override
    public boolean isEmpty() {
        return true;
    }


    @Override
    public LargeInteger at(LargeInteger index) {
        throw new IndexOutOfBoundsException("This selection is empty");
    }


    @Override
    public boolean contains(LargeInteger value) {
        return false;
    }

    @Override
    public ReversibleIterable<LargeInteger> reverseOrder() {
        return ReversibleIterable.of(() -> Collections.emptyIterator(), this);
    }


}
