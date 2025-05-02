package hu.webarticum.holodb.core.benchmark.monotonic;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import hu.webarticum.holodb.core.data.binrel.permutation.BitShufflePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.BitXorPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.FeistelNetworkPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.IdentityPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.ModuloPermutation;
import hu.webarticum.holodb.core.data.binrel.permutation.Permutation;
import hu.webarticum.holodb.core.data.binrel.permutation.PermutationComposition;
import hu.webarticum.holodb.core.data.hasher.FastHasher;
import hu.webarticum.holodb.core.data.hasher.Sha256MacHasher;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PermutationBenchmark {

    private static final LargeInteger SMALL_POW_2 = LargeInteger.of(64);
    private static final LargeInteger SMALL_NON_POW_2 = LargeInteger.of(125);
    private static final LargeInteger BIG_POW_2 = LargeInteger.of("147573952589676412928");
    private static final LargeInteger BIG_NON_POW_2 = LargeInteger.of("287769207549869005209");
    
    
    @Param({
        "Identity", "Modulo", "DirtyFpe", "FeistelFastR1", "FeistelFastR4", "FeistelSha256R2",
        "BitShuffle", "BitXor", "BestComposition",
    })
    private String type;
    

    private Function<LargeInteger, Permutation> factory;
    
    private Permutation instanceSmallPow2;
    private Permutation instanceSmallNonPow2;
    private Permutation instanceBigPow2;
    private Permutation instanceBigNonPow2;
    
    
    @Setup
    public void setup() {
        TreeRandom rootRandom = new HasherTreeRandom("lorem", new FastHasher());
        if (type.equals("Identity")) {
            this.factory = s -> new IdentityPermutation(s);
        } else if (type.equals("Modulo")) {
            this.factory = s -> new ModuloPermutation(rootRandom, s);
        } else if (type.equals("DirtyFpe")) {
            this.factory = s -> new DirtyFpePermutation(rootRandom, s);
        } else if (type.equals("FeistelFastR1")) {
            this.factory = s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 1, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s);
        } else if (type.equals("FeistelFastR4")) {
            this.factory = s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 4, new FastHasher("", (s.bitLength() / 8) + 1)).resized(s);
        } else if (type.equals("FeistelSha256R2")) {
            this.factory = s -> new FeistelNetworkPermutation(rootRandom, s.decrement().bitLength(), 2, new Sha256MacHasher()).resized(s);
        } else if (type.equals("BitShuffle")) {
            this.factory = s -> new BitShufflePermutation(rootRandom, s.decrement().bitLength()).resized(s);
        } else if (type.equals("BitXor")) {
            this.factory = s -> new BitXorPermutation(rootRandom, s.decrement().bitLength()).resized(s);
        } else if (type.equals("BestComposition")) {
            this.factory = s -> new PermutationComposition(
                    new ModuloPermutation(rootRandom, s),
                    new BitShufflePermutation(rootRandom, s.decrement().bitLength()).resized(s),
                    new ModuloPermutation(rootRandom.sub(4324L), s),
                    new BitXorPermutation(rootRandom, s.decrement().bitLength()).resized(s));
        } else {
            throw new IllegalArgumentException("Unknown benchmark type: " + type);
        }

        this.instanceSmallPow2 = this.factory.apply(SMALL_POW_2);
        this.instanceSmallNonPow2 = this.factory.apply(SMALL_NON_POW_2);
        this.instanceBigPow2 = this.factory.apply(BIG_POW_2);
        this.instanceBigNonPow2 = this.factory.apply(BIG_NON_POW_2);
    }
    

    @Benchmark
    public void benchmarkPermutationFactorySmallPow2(Blackhole blackhole) {
        blackhole.consume(factory.apply(SMALL_POW_2));
    }

    @Benchmark
    public void benchmarkPermutationFactoryBigPow2(Blackhole blackhole) {
        blackhole.consume(factory.apply(BIG_POW_2));
    }

    @Benchmark
    public void benchmarkPermutationSmallPow2(Blackhole blackhole) {
        doBenchmarkPermutation(blackhole, instanceSmallPow2);
    }

    @Benchmark
    public void benchmarkPermutationSmallNonPow2(Blackhole blackhole) {
        doBenchmarkPermutation(blackhole, instanceSmallNonPow2);
    }

    @Benchmark
    public void benchmarkPermutationBigPow2(Blackhole blackhole) {
        doBenchmarkPermutation(blackhole, instanceBigPow2);
    }

    @Benchmark
    public void benchmarkPermutationBigNonPow2(Blackhole blackhole) {
        doBenchmarkPermutation(blackhole, instanceBigNonPow2);
    }

    @Benchmark
    public void benchmarkInversePermutationSmallPow2(Blackhole blackhole) {
        doBenchmarkInversePermutation(blackhole, instanceSmallPow2);
    }

    @Benchmark
    public void benchmarkInversePermutationSmallNonPow2(Blackhole blackhole) {
        doBenchmarkInversePermutation(blackhole, instanceSmallNonPow2);
    }

    @Benchmark
    public void benchmarkInversePermutationBigPow2(Blackhole blackhole) {
        doBenchmarkInversePermutation(blackhole, instanceBigPow2);
    }

    @Benchmark
    public void benchmarkInversePermutationBigNonPow2(Blackhole blackhole) {
        doBenchmarkInversePermutation(blackhole, instanceBigNonPow2);
    }
    
    
    private void doBenchmarkPermutation(Blackhole blackhole, Permutation permutation) {
        for (long i = 0; i < 30L; i++) {
            blackhole.consume(permutation.at(LargeInteger.of(i)));
        }
    }
    
    private void doBenchmarkInversePermutation(Blackhole blackhole, Permutation permutation) {
        for (long i = 0; i < 30L; i++) {
            blackhole.consume(permutation.indexOf(LargeInteger.of(i)));
        }
    }

}
