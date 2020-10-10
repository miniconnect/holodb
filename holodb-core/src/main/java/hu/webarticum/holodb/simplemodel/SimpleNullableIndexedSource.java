package hu.webarticum.holodb.simplemodel;

import java.math.BigInteger;
import java.util.Optional;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.Source;

public class SimpleNullableIndexedSource<T> implements IndexedSource<Optional<T>> {

    private final TreeRandom treeRandom;
    
    private final Source<T> baseSource;
    
    private final BigInteger targetSize;
    
    private final BigInteger dataSize;
    

    // FIXME / TODO MonotonicFactory, PermutationFactory ?
    public SimpleNullableIndexedSource(
            TreeRandom treeRandom,
            Source<T> baseSource,
            BigInteger targetSize,
            BigInteger dataSize) {

        this.treeRandom = treeRandom;
        this.baseSource = baseSource;
        this.targetSize = targetSize;
        this.dataSize = dataSize;
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    public Class<Optional<T>> type() {
        return (Class<Optional<T>>) (Object) Optional.class;
    }

    @Override
    public BigInteger size() {
        return targetSize;
    }

    @Override
    public Optional<T> get(BigInteger index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection find(Optional<T> value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection findBetween(Optional<T> minValue, boolean minInclusive, Optional<T> maxValue, boolean maxInclusive) {
        // TODO Auto-generated method stub
        return null;
    }

}
