
package hu.webarticum.holodb.core.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.core.data.binrel.core.Function;

public interface Permutation extends Function {

    public BigInteger indexOf(BigInteger value);
    

    public default Permutation inverted() {
        return new PermutationInverter(this);
    }
    
    public default Permutation resized(BigInteger size) {
        return PermutationUtil.resized(this, size);
    }

}
