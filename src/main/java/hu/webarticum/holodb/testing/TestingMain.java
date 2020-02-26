package hu.webarticum.holodb.testing;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.function.Supplier;

import hu.webarticum.holodb.data.binrel.monotonic.SimpleReducerMonotonic;
import hu.webarticum.holodb.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.data.binrel.permutation.PermutationUtil;
import hu.webarticum.holodb.data.random.DefaultTreeRandom;
import hu.webarticum.holodb.data.random.DefaultTreeRandomOld;
import hu.webarticum.holodb.util.bitsource.BitSource;
import hu.webarticum.holodb.util.bitsource.ByteSource;

public class TestingMain {

    public static void main(String[] args) throws Exception {
        testBitSource();
    }

    public static void testBitSource() throws GeneralSecurityException {
        byte[] initialBuffer = new byte[] { 1, 0 };
        ByteSource byteSource = () -> 89; // 10011010
        BitSource bitSource = new BitSource(initialBuffer, byteSource);
        
        System.out.println(BitSource.byteArrayToString(bitSource.fetch(24)));
    }
    
    public static void testTreeRandom() throws GeneralSecurityException {
        DefaultTreeRandom random = new DefaultTreeRandom();
        System.out.println(random.sub(BigInteger.valueOf(1)).getNumber(BigInteger.valueOf(7)));
    }
    
    public static void testMonotonic() {
        long SIZE = 6L;
        long IMAGE_SIZE = 27L;
        
        SimpleReducerMonotonic monotonic = new SimpleReducerMonotonic(
                new DefaultTreeRandomOld(), BigInteger.valueOf(SIZE), BigInteger.valueOf(IMAGE_SIZE));
        
        for (int i = 0; i < SIZE; i++) {
            System.out.println(String.format("%d: %d", i, monotonic.at(BigInteger.valueOf(i))));
        }
        
        System.out.println();
        System.out.println("------------------------------");
        System.out.println();
        
        for (int i = 0; i < IMAGE_SIZE; i++) {
            if (monotonic.indicesOf(BigInteger.valueOf(i)).getLength().longValue() > 0) {
                System.out.println(String.format("%d found", i));
            }
        }
    }

    public static void testPermutation() throws Exception {
        final byte[] key = "Key lorem ipsum".getBytes();
        BigInteger size = BigInteger.valueOf(12);
        
        Permutation[] permutations = new Permutation[] {
                new DirtyFpePermutation(size, key),
                new IdentityPermutation(size),
                PermutationUtil.resize(new DirtyFpePermutation(size, key), BigInteger.valueOf(7)),
                PermutationUtil.resize(new DirtyFpePermutation(size, key), BigInteger.valueOf(16)),
                };
        
        for (Permutation permutation : permutations) {
            BigInteger permutationSize = permutation.size();
            System.out.println("\n--------------\n" + permutationSize + " - " + permutation.getClass());
            for (BigInteger i = BigInteger.ZERO; i.compareTo(permutationSize) < 0; i = i.add(BigInteger.ONE)) {
                BigInteger enc = permutation.at(i);
                BigInteger dec = permutation.indexOf(enc);
                System.out.println(i + ": " + enc + " " + dec);
            }
        }
    }
    
}
