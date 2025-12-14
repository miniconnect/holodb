package hu.webarticum.holodb.core.lab.hasher.performance;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.lab.util.CommandLineUtil;
import hu.webarticum.holodb.core.lab.util.MutableHolder;
import hu.webarticum.holodb.core.util.ByteUtil;

public class HasherSampleMain {

    public static final String TITLE = "Hasher sample printer";

    private static final byte[] TEST_KEY = "some test key".getBytes(StandardCharsets.UTF_8);


    public static void main(String[] args) {
        CommandLineUtil.printTitle(TITLE);

        MutableHolder<Integer> hashLengthHolder = new MutableHolder<>(256);

        Pair<Integer, Supplier<Hasher>> hasherUserSelection = CommandLineUtil.<Supplier<Hasher>>readOption("Hasher test type", Arrays.asList(
                Pair.of(Sha256MacHasher.class.getSimpleName(), Sha256MacHasher::new),
                Pair.of(NoPreInitDoubleHasher.class.getSimpleName(), () -> new NoPreInitDoubleHasher(TEST_KEY, hashLengthHolder.get())),
                Pair.of(PreInitDoubleHasher.class.getSimpleName(), () -> new PreInitDoubleHasher(TEST_KEY, hashLengthHolder.get())),
                Pair.of(SingleHasher.class.getSimpleName(), () -> new SingleHasher(TEST_KEY, hashLengthHolder.get()))
                ));

        int hasherIndex = hasherUserSelection.getLeft();
        Supplier<Hasher> hasherFactory = hasherUserSelection.getRight();

        if (hasherIndex != 0) {
            hashLengthHolder.set(CommandLineUtil.readIntBetween("Hash length", 1, 1001));
        }

        int dataLength = CommandLineUtil.readIntAtLeast("Length of test data", 1);
        int numberOfSamples = CommandLineUtil.readIntAtLeast("Number of samples", 1);


        Hasher hasher = hasherFactory.get();

        for (int i = 0; i < numberOfSamples; i++) {
            byte[] testData = new byte[dataLength];
            new Random(i).nextBytes(testData);
            byte[] hash = hasher.hash(testData);
            System.out.println(String.format("%s  |  %s", // NOSONAR
                    ByteUtil.bytesToHexadecimalString(hash), ByteUtil.bytesToBinaryString(hash)));
        }
    }

}
