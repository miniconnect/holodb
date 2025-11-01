package hu.webarticum.holodb.core.benchmark.treerandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TreeRandomBenchmark {

    private static final LargeInteger SMALL_MAX = LargeInteger.HUNDRED;

    private static final LargeInteger LARGE_MAX = LargeInteger.of("779014446129734678017646283760917236719327308127");

    private static final LargeInteger SUB_KEY = LargeInteger.of(1567);


    private static final List<LargeInteger> LARGE_KEYS = new ArrayList<>();
    static {
        Random random = new Random(4627834L);
        LargeInteger base = LargeInteger.of("471607071296738748883601912748237460962839471374608179384772384");
        for (int i = 0; i < 30; i++) {
            LARGE_KEYS.add(base.add(base.random(random)));
        }
    }
    
    
    public static void main(String[] args) {
        System.out.println(LARGE_KEYS);
    }
    

    @Param({ "SmallRoot", "LargeRoot" })
    private String rootSeedType;

    @Param({ "Hasher", "OldHasher" })
    private String treeRandomType;
    
    private TreeRandom treeRandom;
    
    
    @Setup
    public void setup() {
        LargeInteger rootSeed;
        if (rootSeedType.equals("LargeRoot")) {
            rootSeed = LargeInteger.of("4628907167081947084628014858734312325929746718981023");
        } else {
            rootSeed = LargeInteger.of(64);
        }
        if (treeRandomType.equals("Hasher")) {
            this.treeRandom = new HasherTreeRandom(rootSeed);
        } else if (treeRandomType.equals("OldHasher")) {
            this.treeRandom = new OldHasherTreeRandom(rootSeed);
        }
    }


    @Benchmark
    public void benchmarkSubBySmallNumbers(Blackhole blackhole) {
        for (long i = 0L; i < 100L; i++) {
            blackhole.consume(treeRandom.sub(i));
        }
    }

    @Benchmark
    public void benchmarkGetSmallNumberBySmallNumbers(Blackhole blackhole) {
        TreeRandom subRandom = treeRandom.sub(SUB_KEY);
        for (long i = 0L; i < 100L; i++) {
            blackhole.consume(subRandom.getNumber(SMALL_MAX));
        }
    }

    @Benchmark
    public void benchmarkGetLargeNumberBySmallNumbers(Blackhole blackhole) {
        TreeRandom subRandom = treeRandom.sub(SUB_KEY);
        for (long i = 0L; i < 100L; i++) {
            blackhole.consume(subRandom.getNumber(LARGE_MAX));
        }
    }

    @Benchmark
    public void benchmarkShortChain(Blackhole blackhole) {
        TreeRandom subRandom = treeRandom;
        for (long i = 10L; i < 15L; i++) {
            subRandom = subRandom.sub(i);
            blackhole.consume(subRandom.getBytes(5));
        }
    }

    @Benchmark
    public void benchmarkLongChain(Blackhole blackhole) {
        TreeRandom subRandom = treeRandom;
        for (long i = 777; i < 1003; i++) {
            subRandom = subRandom.sub(i);
            blackhole.consume(subRandom.getBytes(5));
        }
    }

    @Benchmark
    public void benchmarkMixed(Blackhole blackhole) {
        List<TreeRandom> parentTreeRandoms = new ArrayList<>();
        parentTreeRandoms.add(treeRandom);
        List<TreeRandom> treeRandoms;
        int i = 0;
        do {
            treeRandoms = new ArrayList<>();
            for (TreeRandom parentTreeRandom : parentTreeRandoms) {
                treeRandoms.add(parentTreeRandom.sub(i));
                treeRandoms.add(parentTreeRandom.sub(LARGE_KEYS.get(i)));
            }
            parentTreeRandoms = treeRandoms;
            i++;
        } while (i < 5);
        for (TreeRandom treeRandom : treeRandoms) {
            blackhole.consume(treeRandom.getBytes(5));
            blackhole.consume(treeRandom.getNumber(SMALL_MAX));
            blackhole.consume(treeRandom.getNumber(LARGE_MAX));
        }
    }

}
