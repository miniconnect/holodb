package hu.webarticum.holodb.core.lab.monotonic.distribution;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.LongFunction;

import org.apache.commons.lang3.tuple.Pair;

import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.distribution.ApacheCommonsBinomialSampler;
import hu.webarticum.holodb.core.data.distribution.ExperimentalSampler;
import hu.webarticum.holodb.core.data.distribution.FastSampler;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Hasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.lab.util.CommandLineUtil;
import hu.webarticum.holodb.core.lab.util.MutableHolder;

public class MonotonicDistributionMain {

    public static final String TITLE = "Monotonic distribution";
    
    
    public static void main(String[] args) {
        CommandLineUtil.printTitle(TITLE);

        MutableHolder<TreeRandom> treeRandomHolder = new MutableHolder<>();
        MutableHolder<BinomialMonotonic.SamplerFactory> samplerFactoryHolder = new MutableHolder<>();

        Pair<Integer, BiFunction<Integer, Integer, Monotonic>> monotonicUserSelection = CommandLineUtil.readOption(
                "Monotonic implementation", Arrays.asList(
                        Pair.of(BinomialMonotonic.class.getSimpleName(), (n, k) ->
                                new BinomialMonotonic(treeRandomHolder.get(), samplerFactoryHolder.get(), n, k)),
                        Pair.of(FastMonotonic.class.getSimpleName(), FastMonotonic::new)
                        ));
        int monotonicIndex = monotonicUserSelection.getLeft();
        BiFunction<Integer, Integer, Monotonic> monotonicFactory = monotonicUserSelection.getRight();
        
        if (monotonicIndex == 0) {
            samplerFactoryHolder.set(CommandLineUtil.<BinomialMonotonic.SamplerFactory>readOption(
                    "Sampler implementation", Arrays.asList(
                            Pair.of(ApacheCommonsBinomialSampler.class.getSimpleName(), (seed, size, probability) ->
                                    new ApacheCommonsBinomialSampler(seed, size.intValue(), probability)),
                            Pair.of(FastSampler.class.getSimpleName(), ExperimentalSampler::new),
                            Pair.of(FastSampler.class.getSimpleName(), (seed, size, probability) ->
                                    new FastSampler(size))
                            )).getRight());
        }
        
        LongFunction<Hasher> hasherFactory = CommandLineUtil.<LongFunction<Hasher>>readOption(
                "Hasher implementation", Arrays.asList(
                        Pair.of(Sha256MacHasher.class.getSimpleName(), Sha256MacHasher::new),
                        Pair.of(FastHasher.class.getSimpleName(), FastHasher::new)
                        )).getRight();
        
        long seed = CommandLineUtil.readLong("Seed");
        Hasher hasher = hasherFactory.apply(seed);
        treeRandomHolder.set(new HasherTreeRandom(seed, hasher));

        new QuantityDistributionDisplayer(monotonicFactory).run();
    }

}
