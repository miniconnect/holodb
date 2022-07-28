package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class TransformingSource<T, U> implements Source<U> {

    private final Source<T> baseSource;

    private final Class<? extends U> type;
    
    private final Function<T, U> decoder;
    
    
    public TransformingSource(
            Source<T> baseSource,
            Class<? extends U> type,
            Function<T, U> decoder) {
        this.baseSource = baseSource;
        this.type = type;
        this.decoder = decoder;
    }
    
    
    @Override
    public Class<? extends U> type() {
        return type;
    }

    @Override
    public BigInteger size() {
        return baseSource.size();
    }

    @Override
    public U get(BigInteger index) {
        T encodedValue = baseSource.get(index);
        return encodedValue != null ? decoder.apply(encodedValue) : null;
    }

    @Override
    public Optional<ImmutableList<U>> possibleValues() {
        Optional<ImmutableList<T>> encodedPossibleValues = baseSource.possibleValues();
        return encodedPossibleValues.isPresent() ?
                Optional.of(encodedPossibleValues.get().map(decoder)) :
                Optional.empty();
    }

}
