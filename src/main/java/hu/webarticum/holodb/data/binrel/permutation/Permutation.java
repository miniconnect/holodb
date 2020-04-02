
package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public interface Permutation {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
    public BigInteger indexOf(BigInteger value);
    
}
