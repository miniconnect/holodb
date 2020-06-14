package hu.webarticum.holodb.core.data.binrel.permutation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.core.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.selection.Range;

public class IdentityPermutationTest extends AbstractPermutationTest<IdentityPermutation> {

    @Override
    protected IdentityPermutation create(BigInteger size) {
        return new IdentityPermutation(size);
    }
    
    @Test
    public void testIdentity() {
        int[] sizes = new int[] { 1, 2, 3, 4, 5, 10, 20, 100, 341 };
        for (int size : sizes) {
            checkIdentity(create(BigInteger.valueOf(size)));
        }
    }

    @Test
    public void testProbablyIdentity() {
        BigInteger[] sizes = new BigInteger[] {
            new BigInteger("23645728345"),
            new BigInteger("7016384293457234"),
            new BigInteger("680681618274322737482"),
            new BigInteger("7348758239287593045682879568920"),
        };
        for (BigInteger size : sizes) {
            checkProbablyIdentity(create(size));
        }
    }

    private void checkIdentity(Permutation permutation) {
        Range range = Range.fromLength(BigInteger.ZERO, permutation.size());
        for (BigInteger index : range) {
            BigInteger value = permutation.at(index);
            assertThat(value).isEqualTo(index);
        }
    }

    private void checkProbablyIdentity(Permutation permutation) {
        int numberOfTests = 20;
        BigInteger size = permutation.size();
        BigInteger step = size.divide(BigInteger.valueOf(numberOfTests));
        for (BigInteger index = BigInteger.ZERO; index.compareTo(size) < 0; index = index.add(step)) {
            BigInteger value = permutation.at(index);
            assertThat(value).isEqualTo(index);
        }
    }

}
