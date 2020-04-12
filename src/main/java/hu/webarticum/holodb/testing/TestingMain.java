package hu.webarticum.holodb.testing;

import java.math.BigInteger;
import java.util.Arrays;

import hu.webarticum.holodb.data.binrel.monotonic.BinomialDistributedMonotonic;
import hu.webarticum.holodb.data.random.DefaultTreeRandom;
public class TestingMain {

    public static void main(String[] args) throws Exception {
        testBinomialDistributedMonotonic();
        //testDistribution();
    }
    
    
    private static void testBinomialDistributedMonotonic() {
        for (int seed = 0; seed < 45; seed++) {
            BinomialDistributedMonotonic monotonic = new BinomialDistributedMonotonic(new DefaultTreeRandom(seed), 90, 10);
            for (int i = 0; i < monotonic.size().intValue(); i++) {
                System.out.print(monotonic.at(BigInteger.valueOf(i)) + " ");
            }
            System.out.println();
        }
    }

    public static void testDistribution() {
        new QuantityDistributionDisplayer().run();
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
    
}
