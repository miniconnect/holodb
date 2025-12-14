package hu.webarticum.holodb.core.data.source;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Source<T> {

    public Class<?> type();

    public LargeInteger size();

    public T get(LargeInteger index);

    public Optional<ImmutableList<T>> possibleValues();

}
