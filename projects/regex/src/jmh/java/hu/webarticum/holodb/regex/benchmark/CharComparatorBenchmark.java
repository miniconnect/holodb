package hu.webarticum.holodb.regex.benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.holodb.regex.comparator.DefaultCharComparator;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CharComparatorBenchmark {

    @Param({"=", "?", "2", "5", "a", "á", "ḁ", "ą", "α", "ά", "ú", "ü", "A", "Á", "Ḁ", "Ą", "Ú", "Ü", "Α", "Ά"})
    private char char1;

    @Param({"=", "?", "2", "5", "a", "á", "ḁ", "ą", "α", "ά", "ú", "ü", "A", "Á", "Ḁ", "Ą", "Ú", "Ü", "Α", "Ά"})
    private char char2;


    private CharComparator naturalCharComparator = Character::compare;

    private CharComparator defaultCharComparator = new DefaultCharComparator();


    @Benchmark
    public void benchmarkNaturalCharComparator(Blackhole blackhole) {
        blackhole.consume(naturalCharComparator.compare(char1, char2));
    }

    @Benchmark
    public void benchmarkDefaultCharComparator(Blackhole blackhole) {
        blackhole.consume(defaultCharComparator.compare(char1, char2));
    }

}
