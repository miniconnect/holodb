
package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.Function;

public interface Permutation extends Function {

    public BigInteger indexOf(BigInteger value);
    

    public default Permutation inverted() {
        return new PermutationInverter(this);
    }
    
    public default Permutation resized(BigInteger size) {
        int cmp = size.compareTo(size());
        if (cmp > 0) {
            return new PermutationExtender(this, size);
        } else if (cmp < 0) {
            return new PermutationReducer(this, size);
        } else {
            return this;
        }
    }

}
