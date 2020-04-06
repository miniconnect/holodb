
package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

import hu.webarticum.holodb.data.binrel.core.Function;

public interface Permutation extends Function {

    public BigInteger indexOf(BigInteger value);
    
}
