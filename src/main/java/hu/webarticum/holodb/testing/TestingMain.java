package hu.webarticum.holodb.testing;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import hu.webarticum.holodb.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.data.binrel.monotonic.SimpleRandomExtenderMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.SimpleRandomReducerMonotonic;
import hu.webarticum.holodb.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.data.binrel.permutation.PermutationUtil;
import hu.webarticum.holodb.data.random.DefaultTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.selection.Range;
import hu.webarticum.holodb.data.source.ArraySortedValueSource;
import hu.webarticum.holodb.data.source.MonotonicValueSource;
import hu.webarticum.holodb.data.source.SelectionValueSource;
import hu.webarticum.holodb.data.source.ValueSource;
import hu.webarticum.holodb.util.ByteUtil;
import hu.webarticum.holodb.util.bitsource.BitSource;
import hu.webarticum.holodb.util.bitsource.ByteSource;

public class TestingMain {

    public static void main(String[] args) throws Exception {
        testMonotonicValueSource();
        //testSimpleRandomExtenderMonotonic();
        //testExtenderMonotonicPerformance();
    }
    
    public static void testMonotonicValueSource() {
        TreeSet<String> treeSet = new TreeSet<>(Arrays.asList("alma", "ananasz", "banan", "korte", "meggy", "szolo"));
        ArraySortedValueSource<String> baseSource = new ArraySortedValueSource<>(treeSet);
        Range baseRange = baseSource.findBetween("banan", true, "meggy", true);
        Monotonic monotonic = new FastMonotonic(BigInteger.valueOf(50), baseSource.size());
        MonotonicValueSource<String> wrapperSource = new MonotonicValueSource<String>(baseSource, monotonic);
        Range wrapperRange = wrapperSource.findBetween("banan", true, "meggy", true);

        System.out.println(baseRange);
        dumpValueSource(new SelectionValueSource<String>(baseSource, baseRange));
        System.out.println("----------------------------------");
        System.out.println(wrapperRange);
        dumpValueSource(new SelectionValueSource<String>(wrapperSource, wrapperRange));
        System.out.println("----------------------------------");
        dumpValueSource(wrapperSource);
    }
    
    public static void testArraySortedValueSource() {
        TreeSet<String> treeSet = new TreeSet<>(Arrays.asList("alma", "ananasz", "banan", "korte", "meggy", "szolo"));
        ArraySortedValueSource<String> source = new ArraySortedValueSource<>(treeSet);
        Range range = source.findBetween("banan", true, "meggy", true);
        
        System.out.println(range);
        dumpValueSource(new SelectionValueSource<String>(source, range));
    }
    
    private static void dumpValueSource(ValueSource<?> valueSource) {
        BigInteger size = valueSource.size();
        for (BigInteger i = BigInteger.ZERO; i.compareTo(size) < 0; i = i.add(BigInteger.ONE)) {
            System.out.println(String.format(" > %d. %s", i, valueSource.at(i)));
        }
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
    
    public static void testSimpleRandomReducerMonotonic() {
        int SIZE = 12;
        int IMAGE_SIZE = 51;
        
        for (int k = 0; k < 10; k++) {
            SimpleRandomReducerMonotonic monotonic = new SimpleRandomReducerMonotonic(
                    new DefaultTreeRandom(BigInteger.valueOf(k)), BigInteger.valueOf(SIZE), BigInteger.valueOf(IMAGE_SIZE));

            dumpMonotonic(monotonic);
        }
    }

    public static void testSimpleRandomExtenderMonotonic() {
        int SIZE = 72;
        int IMAGE_SIZE = 13;
        
        for (int k = 0; k < 10; k++) {
            SimpleRandomExtenderMonotonic monotonic = new SimpleRandomExtenderMonotonic(
                    new DefaultTreeRandom(BigInteger.valueOf(k)), BigInteger.valueOf(SIZE), BigInteger.valueOf(IMAGE_SIZE));
            dumpMonotonic(monotonic);
        }
    }

    private static void testFastMonotonic() {
        dumpMonotonic(new FastMonotonic(BigInteger.valueOf(5), BigInteger.valueOf(13)));
        dumpMonotonic(new FastMonotonic(BigInteger.valueOf(13), BigInteger.valueOf(13)));
        dumpMonotonic(new FastMonotonic(BigInteger.valueOf(44), BigInteger.valueOf(13)));
    }
    
    private static void testReducerMonotonicPerformance() {
        TreeRandom treeRandom = new DefaultTreeRandom(BigInteger.valueOf(999));
        
        long millis1 = System.currentTimeMillis();
        
        for (int i = 10; i < 1000; i++) {
            SimpleRandomReducerMonotonic reducer = new SimpleRandomReducerMonotonic(treeRandom, BigInteger.valueOf(i), BigInteger.valueOf(1500));
            for (int j = 0; j < i; j++) {
                BigInteger value = reducer.at(BigInteger.valueOf(j));
                reducer.indicesOf(value);
            }
        }
        
        long millis2 = System.currentTimeMillis();
        
        for (int i = 10; i < 1000; i++) {
            FastMonotonic fastMonotonic = new FastMonotonic(BigInteger.valueOf(i), BigInteger.valueOf(1500));
            for (int j = 0; j < i; j++) {
                BigInteger value = fastMonotonic.at(BigInteger.valueOf(j));
                fastMonotonic.indicesOf(value);
            }
        }

        long millis3 = System.currentTimeMillis();

        System.out.println("T1: " + (millis2 - millis1));
        System.out.println("T2: " + (millis3 - millis2));
    }

    private static void testExtenderMonotonicPerformance() {
        TreeRandom treeRandom = new DefaultTreeRandom(BigInteger.valueOf(999));
        
        long millis1 = System.currentTimeMillis();
        
        for (int i = 1600; i < 3000; i++) {
            SimpleRandomExtenderMonotonic extender = new SimpleRandomExtenderMonotonic(treeRandom, BigInteger.valueOf(i), BigInteger.valueOf(1500));
            for (int j = 0; j < i; j++) {
                BigInteger value = extender.at(BigInteger.valueOf(j));
                extender.indicesOf(value);
            }
        }
        
        long millis2 = System.currentTimeMillis();

        for (int i = 1600; i < 3000; i++) {
            FastMonotonic fastMonotonic = new FastMonotonic(BigInteger.valueOf(i), BigInteger.valueOf(1500));
            for (int j = 0; j < i; j++) {
                BigInteger value = fastMonotonic.at(BigInteger.valueOf(j));
                fastMonotonic.indicesOf(value);
            }
        }

        long millis3 = System.currentTimeMillis();

        System.out.println("T1: " + (millis2 - millis1));
        System.out.println("T2: " + (millis3 - millis2));
    }
    
    private static void dumpMonotonic(Monotonic monotonic) {
        int size = monotonic.size().intValue();
        int imageSize = monotonic.imageSize().intValue();
        
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = monotonic.at(BigInteger.valueOf(i)).intValue();
        }
        
        int[] indirectValues = new int[size];
        for (int i = 0; i < imageSize; i++) {
            Range range = monotonic.indicesOf(BigInteger.valueOf(i));
            int from = range.getFrom().intValue();
            int until = range.getUntil().intValue();
            for (int j = from; j < until; j++) {
                indirectValues[j] = i;
            }
        }

        System.out.println(Arrays.toString(values));
        System.out.println(Arrays.toString(indirectValues));
        System.out.println();
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
