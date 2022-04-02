package hu.webarticum.holodb.core.data.source;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;

public class PermutatedSource<T> implements Source<T> {
    
    private final Source<T> baseSource;
    
    private final Permutation permutation;
    
    
    public PermutatedSource(Source<T> baseSource, Permutation permutation) {
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

}
