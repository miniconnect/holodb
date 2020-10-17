package hu.webarticum.holodb.core.data.binrel.core;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.NoSuchElementException;

public  class FunctionIterator implements Iterator<BigInteger> {

    private final Function function;
    
    private final BigInteger size;
    
    
    private BigInteger counter = BigInteger.ZERO;
    
    private boolean hasNext;
    
    
    public FunctionIterator(Function function) {
        this.function = function;
        this.size = function.size();
        this.hasNext = checkNext();
    }
    
    
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public BigInteger next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        
        BigInteger result = function.at(counter);
        counter = counter.add(BigInteger.ONE);
        hasNext = checkNext();
        return result;
    }
    
    private boolean checkNext() {
        return (counter.compareTo(size) < 0);
    }
    
}