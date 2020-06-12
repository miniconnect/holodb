package hu.webarticum.holodb.demo.hasher.performance;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.data.hasher.Hasher;
import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.demo.util.CommandLineUtil;

public class HasherPerformanceMain {

    public static final String TITLE = "Hasher performance test";

    private static final byte[] TEST_KEY = "some test key".getBytes(StandardCharsets.UTF_8);
    
    
    public static void main(String[] args) {
        CommandLineUtil.printTitle(TITLE);
        
        Supplier<Hasher> hasherFactory = CommandLineUtil.<Supplier<Hasher>>readOption("Hasher test type", Arrays.asList(
                Pair.of(Sha256MacHasher.class.getSimpleName(), Sha256MacHasher::new),
                Pair.of(NoPreInitDoubleHasher.class.getSimpleName(), () -> new NoPreInitDoubleHasher(TEST_KEY, 256)),
                Pair.of(PreInitDoubleHasher.class.getSimpleName(), () -> new PreInitDoubleHasher(TEST_KEY, 256)),
                Pair.of(SingleHasher.class.getSimpleName(), () -> new SingleHasher(TEST_KEY, 256))
                )).getRight();

        int outerLoopSize = CommandLineUtil.readIntAtLeast("Outer loop size", 1);
        int innerLoopSize = CommandLineUtil.readIntAtLeast("Inner loop size", 1);
        int dataLength = CommandLineUtil.readIntAtLeast("Data length", 1);
        
        
        long fullLength = 0;

        byte[] testData = new byte[dataLength];
        new Random(0).nextBytes(testData);
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < outerLoopSize; i++) {
            Hasher hasher = hasherFactory.get();
            for (int j = 0; j < innerLoopSize; j++) {
                fullLength += hasher.hash(testData).length;
            }
        }
        long endTime = System.currentTimeMillis();
        
        long elapsedTime = endTime - startTime;
        double elapsedSeconds = elapsedTime / 1000d;
        
        System.out.println(String.format("%d bytes generated in %.3f seconds", fullLength, elapsedSeconds)); // NOSONAR
    }

}
