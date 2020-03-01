package hu.webarticum.holodb.testing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.data.binrel.monotonic.SimpleReducerMonotonic;
import hu.webarticum.holodb.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.data.binrel.permutation.PermutationUtil;
import hu.webarticum.holodb.data.random.DefaultTreeRandom;
import hu.webarticum.holodb.data.random.DefaultTreeRandomOld;
import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.Range;
import hu.webarticum.holodb.util.bitsource.BitSource;
import hu.webarticum.holodb.util.bitsource.ByteSource;

public class TestingMain {

    public static void main(String[] args) throws Exception {
        testMonotonic();
    }

    public static void testBitSource() throws GeneralSecurityException {
        byte[] initialBuffer = new byte[] { 1, 0 };
        ByteSource byteSource = () -> 89; // 10011010
        BitSource bitSource = new BitSource(initialBuffer, byteSource);
        
        System.out.println(ByteUtil.bytesToString(bitSource.fetch(24)));
    }
    
    public static void testCipher() throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        byte[] keyBytes   = new byte[] { 0, 1, 2, 3, 4, 5 };
        SecretKeySpec key = new SecretKeySpec(keyBytes, "RawBytes");
        mac.init(key);
        
        byte[] data  = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] macBytes = mac.doFinal(data);

        System.out.println("Size: " + macBytes.length);
        System.out.println(ByteUtil.bytesToString(macBytes));
    }
    
    public static void testTreeRandom() {
        int high = 14;
        DefaultTreeRandom random1 = new DefaultTreeRandom(BigInteger.valueOf(2376482));
        DefaultTreeRandom random2 = new DefaultTreeRandom(BigInteger.valueOf(423853));
        int[] counts1 = new int[high];
        int[] counts2 = new int[high];
        for (long i = 0; i < 10000; i++) {
            BigInteger value1 = random1.sub(BigInteger.valueOf(i)).getNumber(BigInteger.valueOf(high));
            BigInteger value2 = random2.sub(BigInteger.valueOf(i)).getNumber(BigInteger.valueOf(high));
            //System.out.println(value1 + " :: " + value2);
            counts1[value1.intValue()]++;
            counts2[value2.intValue()]++;
        }
        System.out.println("----------------------------");
        System.out.println(Arrays.toString(counts1));
        System.out.println(Arrays.toString(counts2));
    }
    
    public static void testMonotonic() {
        int SIZE = 12;
        int IMAGE_SIZE = 51;
        
        for (int k = 0; k < 10; k++) {
            SimpleReducerMonotonic monotonic = new SimpleReducerMonotonic(
                    new DefaultTreeRandom(BigInteger.valueOf(k)), BigInteger.valueOf(SIZE), BigInteger.valueOf(IMAGE_SIZE));
            
            int[] values = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                values[i] = monotonic.at(BigInteger.valueOf(i)).intValue();
            }
            
            int[] indirectValues = new int[SIZE];
            int valueIndex = 0;
            for (int i = 0; i < IMAGE_SIZE; i++) {
                if (monotonic.indicesOf(BigInteger.valueOf(i)).getLength().intValue() > 0) {
                    indirectValues[valueIndex++] = i;
                }
            }
    
            System.out.println(Arrays.toString(values));
            System.out.println(Arrays.toString(indirectValues));
            System.out.println();
        }
    }

    public static void testPermutation() throws Exception {
        final byte[] key = "lorem".getBytes();
        BigInteger size = BigInteger.valueOf(11);
        
        Permutation[] permutations = new Permutation[] {
                new DirtyFpePermutation(key, size),
                new IdentityPermutation(size),
                PermutationUtil.resize(new DirtyFpePermutation(key, size), BigInteger.valueOf(7)),
                PermutationUtil.resize(new DirtyFpePermutation(key, size), BigInteger.valueOf(16)),
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
