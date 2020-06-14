package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

public class PermutationUtil {

    private PermutationUtil() {
    }
    
    
    public static Permutation resize(Permutation base, BigInteger size) {
        if (base instanceof ResizablePermutation) {
            return ((ResizablePermutation) base).resize(size);
        } else if (base instanceof PermutationExtender || base instanceof PermutationReducer) {
            return resize(((PermutationDecorator) base), size);
        }
        
        int cmp = size.compareTo(base.size());
        if (cmp > 0) {
            return new PermutationExtender(base, size);
        } else if (cmp < 0) {
            return new PermutationReducer(base, size);
        } else {
            return base;
        }
    }
    
}
