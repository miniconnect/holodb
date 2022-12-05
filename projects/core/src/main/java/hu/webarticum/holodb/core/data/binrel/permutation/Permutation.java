
package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.binrel.Function;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Permutation extends Function {

    public LargeInteger indexOf(LargeInteger value);
    

    public default Permutation inverted() {
        return new PermutationInverter(this);
    }
    
    public default Permutation resized(LargeInteger size) {
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
