package hu.webarticum.holodb.core.data.selection;

import java.math.BigInteger;
import java.util.Iterator;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.util.IteratorAdapter;

public class PermutatedSelection implements Selection {
    
    private final Selection baseSelection;
    
    private final Permutation permutation;
    
    
    public PermutatedSelection(Selection baseSelection, Permutation permutation) {
        this.baseSelection = baseSelection;
        this.permutation = permutation;
    }
    

    @Override
    public BigInteger size() {
        return baseSelection.size();
    }

    @Override
    public boolean isEmpty() {
        return baseSelection.isEmpty();
    }

    @Override
    public BigInteger at(BigInteger index) {
        return permutation.indexOf(baseSelection.at(index));
    }

    @Override
    public boolean contains(BigInteger value) {
        return baseSelection.contains(permutation.at(value));
    }
    
    @Override
    public Iterator<BigInteger> iterator() {
        return new IteratorAdapter<>(baseSelection.iterator(), this::at);
    }
    
}
