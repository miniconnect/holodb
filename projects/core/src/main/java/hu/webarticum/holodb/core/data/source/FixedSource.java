package hu.webarticum.holodb.core.data.source;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class FixedSource<T extends Comparable<T>> implements Source<T> {

    private Class<T> type;

    private LargeInteger length;

    private Object[] values;


    @SuppressWarnings("unchecked")
    public FixedSource(T... values) {
        this((Class<T>) values.getClass().getComponentType(), Arrays.asList(values));
    }

    public FixedSource(Class<T> type, Collection<T> values) {
        this.type = type;
        this.length = LargeInteger.of(values.size());
        this.values = values.toArray();
    }


    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public LargeInteger size() {
        return length;
    }

    @Override
    public T get(LargeInteger index) {
        @SuppressWarnings("unchecked")
        T result = (T) values[index.intValue()];
        return result;
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return Optional.empty(); // FIXME
    }

}
