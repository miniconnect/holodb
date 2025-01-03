package hu.webarticum.holodb.core.benchmark.monotonic;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import hu.webarticum.holodb.core.data.binrel.monotonic.BinomialMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;
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
    
    
    @Param({"Fast", "Surjective", "Binomial"})
    private String type;
    

    private Monotonic monotonic;
    
    
    @Setup
    public void setup() {
        if (type.equals("Fast")) {
            this.monotonic = new FastMonotonic(SIZE, IMAGE_SIZE);
        } else if (type.equals("Surjective")) {
            this.monotonic = new SurjectiveMonotonic(new HasherTreeRandom(), SIZE, IMAGE_SIZE);
        } else if (type.equals("Binomial")) {
            this.monotonic = new BinomialMonotonic(new HasherTreeRandom(), SIZE, IMAGE_SIZE);
        } else {
            throw new IllegalArgumentException("Unknown benchmark type: " + type);
        }
    }
    

    @Benchmark
    public void benchmarkMonotonic(Blackhole blackhole) {
        LargeInteger size = monotonic.size();
        LargeInteger step = size.divide(DIVIDE);
        for (LargeInteger i = LargeInteger.ZERO; i.isLessThan(size); i = i.add(step)) {
            blackhole.consume(monotonic.at(i));
        }
    }

}
