package hu.webarticum.holodb.core.benchmark.monotonic;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.SurjectiveMonotonic;
import hu.webarticum.holodb.core.data.random.HasherTreeRandom;
import hu.webarticum.miniconnect.lang.LargeInteger;

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MonotonicBenchmark {
    
    private static final LargeInteger SIZE = LargeInteger.of(5000L);
    
    private static final LargeInteger IMAGE_SIZE = LargeInteger.of(2000L);
    
    private static final LargeInteger DIVIDE = LargeInteger.of(20L);
    

    private FastMonotonic fastMonotonic;

    private SurjectiveMonotonic surjectiveMonotonic;
    
    private BinomialMonotonic binomialMonotonic;
    
    
    @Setup
    public void setup() {
        fastMonotonic = new FastMonotonic(SIZE, IMAGE_SIZE);
        surjectiveMonotonic = new SurjectiveMonotonic(new HasherTreeRandom(), SIZE, IMAGE_SIZE);
        binomialMonotonic = new BinomialMonotonic(new HasherTreeRandom(), SIZE, IMAGE_SIZE);
    }
    

    @Benchmark
    public void benchmarkFastMonotonic(Blackhole blackhole) {
        LargeInteger size = fastMonotonic.size();
        LargeInteger step = size.divide(DIVIDE);
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(step)) {
            blackhole.consume(fastMonotonic.at(i));
        }
    }

    @Benchmark
    public void benchmarkSurjectiveMonotonic(Blackhole blackhole) {
        LargeInteger size = surjectiveMonotonic.size();
        LargeInteger step = size.divide(DIVIDE);
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(step)) {
            blackhole.consume(surjectiveMonotonic.at(i));
        }
    }

    @Benchmark
    public void benchmarkBinomialMonotonic(Blackhole blackhole) {
        LargeInteger size = binomialMonotonic.size();
        LargeInteger step = size.divide(DIVIDE);
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(step)) {
            blackhole.consume(binomialMonotonic.at(i));
        }
    }

}
