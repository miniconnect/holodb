package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.PermutatedSelection;
import hu.webarticum.holodb.core.data.selection.Selection;

public class PermutatedIndexedSource<T> implements IndexedSource<T> {
    
    private final IndexedSource<T> baseSource;
    
    private final Permutation permutation;
    
    
    public PermutatedIndexedSource(IndexedSource<T> baseSource, Permutation permutation) {
        BigInteger baseSourceSize = baseSource.size();
        BigInteger permutationSize = permutation.size();
        if (baseSourceSize != permutationSize) {
            throw new IllegalArgumentException(String.format(
                    "Unmatching sizes (baseSource size: %d, permutation size: %d)",
                    baseSourceSize,
                    permutationSize));
        }
        
        this.baseSource = baseSource;
        this.permutation = permutation;
    }
    

    @Override
    public Class<T> type() {
        return baseSource.type();
    }

    @Override
    public BigInteger size() {
        return baseSource.size();
    }

    @Override
    public T get(BigInteger index) {
        BigInteger permutatedIndex = permutation.at(index);
        return baseSource.get(permutatedIndex);
    }

    @Override
    public Selection find(Object value) {
        Selection baseSelection = baseSource.find(value);
        return new PermutatedSelection(baseSelection, permutation);
    }
    
    @Override
    public Selection findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        Selection baseSelection =
                baseSource.findBetween(minValue, minInclusive, maxValue, maxInclusive);
        return new PermutatedSelection(baseSelection, permutation);
    }
    
    @Override
    public Selection findNulls() {
        Selection baseSelection = baseSource.findNulls();
        return new PermutatedSelection(baseSelection, permutation);
    }
    
}
