package hu.webarticum.holodb.core.data.binrel;

import java.util.Iterator;

import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Function extends Iterable<LargeInteger> {

    public LargeInteger size();

    public LargeInteger at(LargeInteger index);


    @Override
    public default Iterator<LargeInteger> iterator() {
        return new FunctionIterator(this);
    }

}
