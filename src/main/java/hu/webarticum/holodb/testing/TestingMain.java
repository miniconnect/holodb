package hu.webarticum.holodb.testing;

import java.math.BigInteger;
import java.util.Arrays;

import hu.webarticum.holodb.data.binrel.monotonic.BinomialDistributedMonotonic;
import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.data.random.HasherTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.selection.Range;
import hu.webarticum.holodb.util.bitsource.ByteSource;
import hu.webarticum.holodb.util.bitsource.FastByteSource;
import hu.webarticum.holodb.util.bitsource.JavaRandomByteSource;
public class TestingMain {

    public static void main(String[] args) throws Exception {
        //testBinomialDistributedMonotonic();
        //testDistribution();
        testByteSources();
    }
    
    
    private static void testByteSources() {
        ByteSource[] byteSources = new ByteSource[] {
                () -> (byte) 0,
                new FastByteSource(),
                new JavaRandomByteSource(),
        };
        
        for (ByteSource byteSource : byteSources) {
            System.out.println(byteSource.getClass().getSimpleName() + ":");
            for (int i = 0; i < 3; i++) {
                long start = System.currentTimeMillis();
                for (long j = 0; j < 1000000000L; j++) {
                    byteSource.next();
                }
                long end = System.currentTimeMillis();
                System.out.println(((end - start) / 1000d));
            }
            System.out.println("----------------------");
        }
    }
    
    private static void testBinomialDistributedMonotonic() {
        long size = 90;
        long imageSize = 10;
        for (int seed = 0; seed < 3; seed++) {
            TreeRandom treeRandom = new HasherTreeRandom(new Sha256MacHasher());
            BinomialDistributedMonotonic monotonic = new BinomialDistributedMonotonic(treeRandom, size, imageSize);
            for (int i = 0; i < size; i++) {
                System.out.print(monotonic.at(BigInteger.valueOf(i)) + " ");
            }
            System.out.println();
            for (int i = 0; i < imageSize; i++) {
                Range range = monotonic.indicesOf(BigInteger.valueOf(i));
                int from = range.getFrom().intValue();
                int until = range.getUntil().intValue();
                for (int j = 0; j < from; j++) {
                    System.out.print("  ");
                }
                for (int j = from; j < until; j++) {
                    System.out.print(i + " ");
                }
                System.out.println();
            }
            System.out.println();
            for (int i = 0; i < 180; i++) {
                System.out.print('-');
            }
            System.out.println();
            System.out.println();
        }
    }

    public static void testDistribution() {
        long start = System.currentTimeMillis();
        new QuantityDistributionDisplayer().run();
        long end = System.currentTimeMillis();
        System.out.println(String.format("Elapsed: %f", (end - start) / 1000.0));
    }
    
    public static void testTreeRandom() {
        int high = 14;
        TreeRandom random1 = new HasherTreeRandom(2376482L);
        TreeRandom random2 = new HasherTreeRandom(423853L);
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
    
}
