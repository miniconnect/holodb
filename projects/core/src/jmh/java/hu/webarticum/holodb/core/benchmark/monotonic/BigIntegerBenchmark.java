package hu.webarticum.holodb.core.benchmark.monotonic;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import scala.math.BigInt;

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

@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class BigIntegerBenchmark {
    
    private Long[] longValues;
    
    private BigInteger[] bigIntegerValues;
    
    private BigInt[] scalaBigIntValues;
    
    
    @Setup
    public void setup() {
        Random random = new Random();
        longValues = new Long[] {
                random.nextLong(1000L) + 200L,
                random.nextLong(20L) + 10L,
                random.nextLong(10000L) + 2000L,
                random.nextLong(100L) + 50L,
        };
        
        bigIntegerValues = new BigInteger[longValues.length];
        for (int i = 0; i < longValues.length; i++) {
            bigIntegerValues[i] = BigInteger.valueOf(longValues[i]);
        }
        
        scalaBigIntValues = new BigInt[longValues.length];
        for (int i = 0; i < longValues.length; i++) {
            scalaBigIntValues[i] = BigInt.apply(longValues[i]);
        }
    }
    
    
    @Benchmark
    public void benchmarkLong(Blackhole blackhole) {
        blackhole.consume(((longValues[0] * longValues[1]) + longValues[2]) / longValues[3]);
    }

    @Benchmark
    public void benchmarkBigInteger(Blackhole blackhole) {
        blackhole.consume(
                bigIntegerValues[0]
                        .multiply(bigIntegerValues[1])
                        .add(bigIntegerValues[2])
                        .divide(bigIntegerValues[3]));
    }

    @Benchmark
    public void benchmarkScalaBigInt(Blackhole blackhole) {
        blackhole.consume(
                scalaBigIntValues[0]
                        .$times(scalaBigIntValues[1])
                        .$plus(scalaBigIntValues[2])
                        .$div(scalaBigIntValues[3]));
    }
    
}
