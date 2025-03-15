package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class PermutationComposition implements Permutation {
    
    private final ImmutableList<Permutation> permutations;
    
    
    public PermutationComposition(Permutation... permutations) {
        this(ImmutableList.of(permutations));
    }

    public PermutationComposition(ImmutableList<Permutation> permutations) {
        if (permutations.isEmpty()) {
            throw new IllegalArgumentException("At least one permutation is required for composition");
        }
        LargeInteger size = permutations.get(0).size();
        int count = permutations.size();
        for (int i = 1; i < count; i++) {
            LargeInteger itemSize = permutations.get(i).size();
            if (!itemSize.isEqualTo(size)) {
                throw new IllegalArgumentException(String.format(
                        "Unmatching permutation sizes (%s != %s)", size, itemSize));
            }
        }
        
        this.permutations = permutations;
    }
    

    @Override
    public LargeInteger size() {
        return permutations.get(0).size();
    }

    @Override
    public LargeInteger at(LargeInteger index) {
        LargeInteger result = index;
        for (Permutation permutation : permutations.reverseOrder()) {
            result = permutation.at(result);
        }
        return result;
    }

    @Override
    public LargeInteger indexOf(LargeInteger value) {
        LargeInteger result = value;
        for (Permutation permutation : permutations) {
            result = permutation.indexOf(result);
        }
        return result;
    }
    
}
