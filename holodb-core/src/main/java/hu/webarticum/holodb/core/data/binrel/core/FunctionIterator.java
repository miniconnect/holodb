package hu.webarticum.holodb.core.data.binrel.core;

import java.math.BigInteger;
import java.util.Iterator;

public  class FunctionIterator implements Iterator<BigInteger> {

    private final Function function;
    
    private final BigInteger size;
    
    
    private BigInteger counter = BigInteger.ZERO;
    
    
    public FunctionIterator(Function function) {
        this.function = function;
        this.size = function.size();
    }
    
    
    @Override
    public boolean hasNext() {
        return (counter.compareTo(size) < 0);
    }

    @Override
    public BigInteger next() {

        // FIXME: unnecessary overhead
        //if (!hasNext()) {
        //    throw new NoSuchElementException();
        //}
        
        return function.at(counter);
    }
    
}