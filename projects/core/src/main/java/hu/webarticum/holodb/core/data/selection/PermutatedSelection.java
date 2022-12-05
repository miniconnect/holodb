package hu.webarticum.holodb.core.data.selection;

import java.util.Iterator;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.miniconnect.lang.ReversibleIterable;
import hu.webarticum.miniconnect.util.IteratorAdapter;

public class PermutatedSelection implements Selection {
    
    private final Selection baseSelection;
    
    private final Permutation permutation;
    
    
    public PermutatedSelection(Selection baseSelection, Permutation permutation) {
        this.baseSelection = baseSelection;
        this.permutation = permutation;
    }
    

    @Override
    public LargeInteger size() {
        return baseSelection.size();
    }

    @Override
    public boolean isEmpty() {
        return baseSelection.isEmpty();
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        return permutation.at(baseSelection.at(index));
    }

    @Override
    public boolean contains(LargeInteger value) {
        return baseSelection.contains(permutation.indexOf(value));
    }
    
    @Override
    public Iterator<LargeInteger> iterator() {
        return new IteratorAdapter<>(baseSelection.iterator(), permutation::at);
    }

    @Override
    public ReversibleIterable<LargeInteger> reverseOrder() {
        Iterable<LargeInteger> reversedBase = baseSelection.reverseOrder();
        Iterable<LargeInteger> permutatedReversed =
                () -> new IteratorAdapter<>(reversedBase.iterator(), permutation::at);
        return ReversibleIterable.of(permutatedReversed, this);
    }
    
}
