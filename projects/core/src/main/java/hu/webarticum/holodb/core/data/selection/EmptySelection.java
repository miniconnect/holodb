package hu.webarticum.holodb.core.data.selection;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;

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
    public Iterator<BigInteger> iterator() {
        return Collections.emptyIterator();
    }


    @Override
    public BigInteger size() {
        return BigInteger.ZERO;
    }


    @Override
    public boolean isEmpty() {
        return true;
    }


    @Override
    public BigInteger at(BigInteger index) {
        throw new IndexOutOfBoundsException("This selection is empty");
    }


    @Override
    public boolean contains(BigInteger value) {
        return false;
    }

    @Override
    public ReversibleIterable<BigInteger> reverseOrder() {
        return ReversibleIterable.of(() -> Collections.emptyIterator(), this);
    }


}
