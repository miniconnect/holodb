package hu.webarticum.holodb.lab.monotonic.distribution;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.data.binrel.monotonic.BinomialDistributedMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.data.binrel.monotonic.TestMonotonic;
import hu.webarticum.holodb.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.data.random.HasherTreeRandom;
import hu.webarticum.holodb.data.random.TreeRandom;
import hu.webarticum.holodb.lab.util.CommandLineUtil;
import hu.webarticum.holodb.lab.util.MutableHolder;

public class MonotonicDistributionMain {

    public static final String TITLE = "Monotonic distribution";
    
    
    public static void main(String[] args) {
        CommandLineUtil.printTitle(TITLE);
        
        MutableHolder<TreeRandom> treeRandomHolder = new MutableHolder<>();

        Pair<Integer, BiFunction<Integer, Integer, Monotonic>> monotonicUserSelection = CommandLineUtil.readOption("Monotonic implementation", Arrays.asList(
                Pair.of(BinomialDistributedMonotonic.class.getSimpleName(), (n, k) -> new BinomialDistributedMonotonic(treeRandomHolder.get(), n, k)),
                Pair.of(TestMonotonic.class.getSimpleName(), (n, k) -> new TestMonotonic(treeRandomHolder.get(), n, k)),
                Pair.of(FastMonotonic.class.getSimpleName(), FastMonotonic::new)
                ));
        int monotonicIndex = monotonicUserSelection.getLeft();
        BiFunction<Integer, Integer, Monotonic> monotonicFactory = monotonicUserSelection.getRight();
        
        if (monotonicIndex != 2) {
            long seed = CommandLineUtil.readLong("Seed");
            treeRandomHolder.set(new HasherTreeRandom(seed, new Sha256MacHasher()));
        }

        new QuantityDistributionDisplayer(monotonicFactory).run();
    }

}
