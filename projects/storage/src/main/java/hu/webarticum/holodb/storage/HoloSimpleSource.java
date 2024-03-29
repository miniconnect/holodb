package hu.webarticum.holodb.storage;

import java.util.Optional;

import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.MonotonicSource;
import hu.webarticum.holodb.core.data.source.PermutatedIndexedSource;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class HoloSimpleSource<T> implements Source<T> {
    
    private final IndexedSource<T> indexedSource;
    
    
    public HoloSimpleSource(TreeRandom treeRandom, SortedSource<T> baseSource, LargeInteger size) {
        MonotonicSource<T> monotonicSource = new MonotonicSource<>(
                baseSource,
                new BinomialMonotonic(treeRandom.sub("monotonic"), size, baseSource.size()));
        Permutation permutation = new DirtyFpePermutation(treeRandom.sub("permutation"), size);
        this.indexedSource = new PermutatedIndexedSource<>(monotonicSource, permutation);
    }
    

    @Override
    public Class<?> type() {
        return indexedSource.type();
    }

    @Override
    public LargeInteger size() {
        return indexedSource.size();
    }

    @Override
    public T get(LargeInteger index) {
        return indexedSource.get(index);
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return indexedSource.possibleValues();
    }
    
    public TableIndex createIndex(String indexName, String columnName) {
        return new IndexTableIndex(indexName, columnName, indexedSource);
    }

}
