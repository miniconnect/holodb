
package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

// TODO: separated ReversiblePermutation interface?
//         or: always reversible?

public interface Permutation {

    public BigInteger size();
    
    public BigInteger at(BigInteger index);
    
    public boolean isReversible();

    /** @throws UnsupportedOperationException */
    public BigInteger indexOf(BigInteger value);
    
}
