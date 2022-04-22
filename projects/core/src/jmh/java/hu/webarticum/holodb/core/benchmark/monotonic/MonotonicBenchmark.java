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
import org.openjdk.jmh.infra.Blackhole;

import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.monotonic.Monotonic;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MonotonicBenchmark {
    
    private Monotonic monotonic;
    
    
    @Setup
    public void setup() {
        monotonic = new FastMonotonic(BigInteger.valueOf(100000L), BigInteger.valueOf(35000000L));
    }
    
    
    @Benchmark
    @Fork(value = 2, warmups = 2)
    public void someBenchmark(Blackhole blackhole) {
        BigInteger size = monotonic.size();
        BigInteger step = size.divide(BigInteger.valueOf(100L));
        for (BigInteger i = BigInteger.ZERO; i.compareTo(size) < 0; i = i.add(step)) {
            blackhole.consume(monotonic.at(i));
        }
    }
    
}
