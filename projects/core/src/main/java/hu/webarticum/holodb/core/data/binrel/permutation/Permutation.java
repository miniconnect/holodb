
package hu.webarticum.holodb.core.data.binrel.permutation;

import hu.webarticum.holodb.core.data.binrel.Function;
import hu.webarticum.miniconnect.lang.LargeInteger;

public interface Permutation extends Function {

    public LargeInteger indexOf(LargeInteger value);


    public default Permutation inverted() {
        return new PermutationInverter(this);
    }

    public default Permutation resized(LargeInteger newSize) {
        LargeInteger size = size();
        int cmp = newSize.compareTo(size);
        if (cmp == 0) {
            return this;
        } else if (cmp < 0) {
            return new PermutationReducer(this, newSize);
        }

        LargeInteger mod = newSize.mod(size);
        if (mod.isZero()) {
            return new PermutationRepeater(this, newSize);
        }

        LargeInteger sureSize = newSize.subtract(mod).add(size);
        return new PermutationReducer(new PermutationRepeater(this, sureSize), newSize);
    }

}
