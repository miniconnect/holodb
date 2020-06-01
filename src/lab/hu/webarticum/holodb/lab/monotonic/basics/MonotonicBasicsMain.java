package hu.webarticum.holodb.lab.monotonic.basics;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.data.binrel.monotonic.BinomialDistributedMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.data.binrel.monotonic.TestMonotonic;
import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.data.random.HasherTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.data.selection.Range;
import hu.webarticum.holodb.lab.util.CommandLineUtil;
import hu.webarticum.holodb.lab.util.MutableHolder;

public class MonotonicBasicsMain {

    public static final String TITLE = "Monotonic basic demo";
    

    public static void main(String[] args) {
        CommandLineUtil.printTitle(TITLE);
        
        MutableHolder<TreeRandom> treeRandomHolder = new MutableHolder<>();
        MutableHolder<Integer> sizeHolder = new MutableHolder<>();
        MutableHolder<Integer> imageSizeHolder = new MutableHolder<>();
        
        Pair<Integer, Supplier<Monotonic>> monotonicUserSelection = CommandLineUtil.readOption("Monotonic implementation", Arrays.asList(
                Pair.of(BinomialDistributedMonotonic.class.getSimpleName(), () -> new BinomialDistributedMonotonic(
                        treeRandomHolder.get(), sizeHolder.get(), imageSizeHolder.get())),
                Pair.of(TestMonotonic.class.getSimpleName(), () -> new TestMonotonic(treeRandomHolder.get(), sizeHolder.get(), imageSizeHolder.get())),
                Pair.of(FastMonotonic.class.getSimpleName(), () -> new FastMonotonic(sizeHolder.get(), imageSizeHolder.get()))
                ));
        int monotonicIndex = monotonicUserSelection.getLeft();
        Supplier<Monotonic> monotonicFactory = monotonicUserSelection.getRight();
        
        int size = CommandLineUtil.readInt("Monotonic size");
        sizeHolder.set(size);
        
        int imageSize = CommandLineUtil.readInt("Monotonic image size");
        imageSizeHolder.set(imageSize);

        if (monotonicIndex != 2) {
            long seed = CommandLineUtil.readLong("Seed");
            treeRandomHolder.set(new HasherTreeRandom(seed, new Sha256MacHasher()));
        }

        
        CommandLineUtil.printSeparator();
        
        
        Monotonic monotonic = monotonicFactory.get();

        int columnWidth = ("" + (imageSize - 1)).length() + 1;
        for (int i = 0; i < size; i++) {
            BigInteger value = monotonic.at(BigInteger.valueOf(i));
            System.out.print(StringUtils.leftPad(value.toString(), columnWidth)); // NOSONAR
        }
        for (int i = 0; i < imageSize; i++) {
            Range range = monotonic.indicesOf(BigInteger.valueOf(i));
            int from = range.getFrom().intValue();
            int until = range.getUntil().intValue();
            if (!range.isEmpty()) {
                System.out.println(); // NOSONAR
                for (int j = 0; j < from; j++) {
                    System.out.print(StringUtils.leftPad("", columnWidth)); // NOSONAR
                }
            }
            for (int j = from; j < until; j++) {
                System.out.print(StringUtils.leftPad(Long.toString(i), columnWidth)); // NOSONAR
            }
        }
        System.out.println(); // NOSONAR
    }
    
}
