package hu.webarticum.holodb.core.data.binrel;

import java.util.Iterator;
import java.util.NoSuchElementException;

import hu.webarticum.miniconnect.lang.LargeInteger;

public class FunctionIterator implements Iterator<LargeInteger> {

    private final Function function;

    private final LargeInteger size;


    private LargeInteger counter = LargeInteger.ZERO;

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
    public LargeInteger next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        LargeInteger result = function.at(counter);
        counter = counter.increment();
        hasNext = checkNext();
        return result;
    }

    private boolean checkNext() {
        return counter.isLessThan(size);
    }

}
