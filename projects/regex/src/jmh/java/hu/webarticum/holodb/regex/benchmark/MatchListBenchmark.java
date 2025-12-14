package hu.webarticum.holodb.regex.benchmark;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.mifmif.common.regex.Generex;

import hu.webarticum.holodb.regex.facade.MatchList;
import nl.flotsam.xeger.Xeger;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MatchListBenchmark {

    @Param({"lorem", "[0-9]{10}", "f{0,2}[ra](t[tu]|tue?)s?", "[0-9]*[xyz]?(lorem|lo[rp][ea][mn]u)u?[0-9]*"})
    private String pattern;

    private MatchList matchList;

    private Generex generex;

    private Xeger xeger;

    @Setup(Level.Iteration)
    public void setup() {
        matchList = createMatchList();
        generex = createGenerex();
        xeger = createXeger();
    }

    @Benchmark
    public void benchmarkSetupMatchList(Blackhole blackhole) {
        blackhole.consume(createMatchList());
    }

    @Benchmark
    public void benchmarkSetupGenerex(Blackhole blackhole) {
        blackhole.consume(createGenerex());
    }

    @Benchmark
    public void benchmarkSetupXeger(Blackhole blackhole) {
        blackhole.consume(createXeger());
    }

    @Benchmark
    public void benchmarkRandomMatchList(Blackhole blackhole) {
        blackhole.consume(matchList.random());
    }

    @Benchmark
    public void benchmarkRandomGenerex(Blackhole blackhole) {
        blackhole.consume(generex.random());
    }

    @Benchmark
    public void benchmarkRandomXeger(Blackhole blackhole) {
        blackhole.consume(xeger.generate());
    }

    private MatchList createMatchList() {
        return MatchList.builder().random(createRandom()).build(pattern);
    }

    private Generex createGenerex() {
        return new Generex(pattern, createRandom());
    }

    private Xeger createXeger() {
        return new Xeger(pattern, createRandom());
    }

    private Random createRandom() {
        return new Random(42);
    }

}
