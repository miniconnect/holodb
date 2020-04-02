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
import hu.webarticum.holodb.data.binrel.monotonic.DefaultRandomExtenderMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.DefaultRandomReducerMonotonic;
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
        collectQuantityStatistic();
    }
    
    public static void collectQuantityStatistic() {
        int n = 1000;
        int k = 100;
        int start = 0;
        int end = 25;
        int w = 35;
        int h = 15;
        
        Random random = new Random(0);
        int[] counts = new int[k];
        for (int i = 0; i < n; i++) {
            counts[random.nextInt(k)]++;
        }
        
        int[] statistics = new int[end - start];
        for (int i = 0; i < k; i++) {
            int count = counts[start + i];
            if (count >= start && count < end) {
                System.out.println(i + "!");
                statistics[count - start]++;
            }
        }

        System.out.println(start + " <--> " + end);
        System.out.println(Arrays.toString(counts));
        System.out.println(Arrays.toString(statistics));
        
        printDiagram(statistics, w, h);
    }
    
    private static void printDiagram(int[] values, int w, int h) {
        int length = values.length;
        int s = (int) Math.ceil((double) length / w);
        System.out.println("s: " + s);
        double max = 0;
        double[] avgs = new double[w];
        for (int i = 0; i < w; i++) {
            int start = i * s;
            int end = start + s;
            int ss = end > length ? s - (end - length) : s;
            int sum = 0;
            for (int j = 0; j < ss; j++) {
                sum += values[start + j];
            }
            double avg = (double) sum / ss;
            if (avg > max) {
                max = avg;
            }
            avgs[i] = avg;
        }
        double scale = h / max;
        
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                double value = avgs[j] * scale;
                System.out.print(value < h - i ? ' ' : '#');
            }
            System.out.println();
        }
        for (int i = 0; i < w; i++) {
            System.out.print('_');
        }
        System.out.println();
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
