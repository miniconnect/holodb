package hu.webarticum.holodb.simplemodel;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.Source;

public class SimpleIndexedSource<T> implements IndexedSource<T> {
    
    private final TreeRandom treeRandom;
    
    private final Source<T> baseSource;
    
    private final BigInteger targetSize;
    
    
    // FIXME / TODO MonotonicFactory, PermutationFactory ?
    public SimpleIndexedSource(
            TreeRandom treeRandom,
            Source<T> baseSource,
            BigInteger targetSize) {

        this.treeRandom = treeRandom;
        this.baseSource = baseSource;
        this.targetSize = targetSize;
    }
    
    
    @Override
    public Class<T> type() {
        return baseSource.type();
    }

    @Override
    public BigInteger size() {
        return targetSize;
    }

    @Override
    public T get(BigInteger index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection find(T value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Selection findBetween(T minValue, boolean minInclusive, T maxValue, boolean maxInclusive) {
        // TODO Auto-generated method stub
        return null;
    }

}
