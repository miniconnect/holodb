package hu.webarticum.holodb.data.binrel.permutation;

import java.math.BigInteger;

public class IdentityPermutationTest extends AbstractPermutationTest<IdentityPermutation> {

    @Override
    protected IdentityPermutation create(BigInteger size) {
        return new IdentityPermutation(size);
    }
    
    public void testIdentity() {
        int[] sizes = new int[] { 1, 2, 3, 4, 5, 10, 20, 100, 341 };
        for (int size : sizes) {
            checkIdentity(create(BigInteger.valueOf(size)));
        }
    }
    
    private void checkIdentity(Permutation permutation) {
        
        // TODO:
        // make Selection and Range Iterable<BigInteger>
        // iterate over Range of this permutation
        
    }

}
