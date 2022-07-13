package hu.webarticum.holodb.core.benchmark.monotonic;

import java.math.BigInteger;
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

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MonotonicBenchmark {
    
    private static final BigInteger SIZE = BigInteger.valueOf(5000L);
    
    private static final BigInteger IMAGE_SIZE = BigInteger.valueOf(2000L);
    
    private static final BigInteger DIVIDE = BigInteger.valueOf(20L);
    

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
        BigInteger size = fastMonotonic.size();
        BigInteger step = size.divide(DIVIDE);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(size) < 0; i = i.add(step)) {
            blackhole.consume(fastMonotonic.at(i));
        }
    }

    @Benchmark
    public void benchmarkSurjectiveMonotonic(Blackhole blackhole) {
        BigInteger size = surjectiveMonotonic.size();
        BigInteger step = size.divide(DIVIDE);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(size) < 0; i = i.add(step)) {
            blackhole.consume(surjectiveMonotonic.at(i));
        }
    }

    @Benchmark
    public void benchmarkBinomialMonotonic(Blackhole blackhole) {
        BigInteger size = binomialMonotonic.size();
        BigInteger step = size.divide(DIVIDE);
        for (BigInteger i = BigInteger.ZERO; i.compareTo(size) < 0; i = i.add(step)) {
            blackhole.consume(binomialMonotonic.at(i));
        }
    }

}
