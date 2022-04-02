package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;

public class FixedSource<T extends Comparable<T>> implements Source<T> {

    private Class<T> type;

    private BigInteger length;
    
    private Object[] values;
    

    @SuppressWarnings("unchecked")
    public FixedSource(T... values) {
        this((Class<T>) values.getClass().getComponentType(), Arrays.asList(values));
    }
    
    public FixedSource(Class<T> type, Collection<T> values) {
        this.type = type;
        this.length = BigInteger.valueOf(values.size());
        this.values = values.toArray();
    }
    
    
    @Override
    public Class<T> type() {
        return type;
    }
    
    @Override
    public BigInteger size() {
        return length;
    }

    @Override
    public T get(BigInteger index) {
        @SuppressWarnings("unchecked")
        T result = (T) values[index.intValue()];
        return result;
    }

}
