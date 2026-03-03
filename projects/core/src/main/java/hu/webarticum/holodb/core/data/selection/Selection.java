package hu.webarticum.holodb.core.data.selection;

import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ReversibleIterable;

public interface Selection extends ReversibleIterable<LargeInteger> {

    public LargeInteger size();

    public boolean isEmpty();

    public LargeInteger at(LargeInteger index);

    public boolean contains(LargeInteger value);

}
