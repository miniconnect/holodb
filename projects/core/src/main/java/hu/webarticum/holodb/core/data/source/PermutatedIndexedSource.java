package hu.webarticum.holodb.core.data.source;

import java.util.Comparator;
import java.util.Optional;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.PermutatedSelection;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutatedIndexedSource<T> implements IndexedSource<T> {

    private final IndexedSource<T> baseSource;

    private final Permutation permutation;


    public PermutatedIndexedSource(IndexedSource<T> baseSource, Permutation permutation) {
        LargeInteger baseSourceSize = baseSource.size();
        LargeInteger permutationSize = permutation.size();
        if (!baseSourceSize.equals(permutationSize)) {
            throw new IllegalArgumentException(String.format(
                    "Unmatching sizes (baseSource size: %d, permutation size: %d)",
                    baseSourceSize.bigIntegerValue(),
                    permutationSize.bigIntegerValue()));
        }

        this.baseSource = baseSource;
        this.permutation = permutation;
    }


    @Override
    public Class<?> type() {
        return baseSource.type();
    }

    @Override
    public LargeInteger size() {
        return baseSource.size();
    }

    @Override
    public T get(LargeInteger index) {
        LargeInteger permutatedIndex = permutation.indexOf(index);
        return baseSource.get(permutatedIndex);
    }

    @Override
    public Comparator<?> comparator() {
        return baseSource.comparator();
    }

    @Override
    public Optional<ImmutableList<T>> possibleValues() {
        return baseSource.possibleValues();
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
