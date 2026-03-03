package hu.webarticum.holodb.regex.benchmark;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
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

import hu.webarticum.holodb.regex.comparator.CharStringComparator;
import hu.webarticum.holodb.regex.comparator.DefaultCharComparator;
import hu.webarticum.miniconnect.lang.ImmutableList;

@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringComparatorBenchmark {

    private static final ImmutableList<String> SIMPLE_STRINGS = ImmutableList.of(
            "z~d", "KLM", "~", "3,", "XYZ", "a", "!B", "zad", "ab", "!,", "~", "zaD", "abba", "~", "9A",
            "xyz", "z~d", "3=", "b~", "klm", "ABBA", "b~", "", "b~r", "KLM", "!5", "", "A")
            .map(s -> Normalizer.normalize(s, Normalizer.Form.NFC));

    private static final ImmutableList<String> MIXED_STRINGS = ImmutableList.of(
            "zḁd", "KLM", "ą", "3,", "XYZ", "a", "!B", "zad", "ab", "!,", "ą", "zaD", "abba", "ḁ", "9A",
            "xyz", "zḁd", "3=", "bą", "klm", "ABBA", "bą", "", "bąr", "KLM", "!5", "", "A")
            .map(s -> Normalizer.normalize(s, Normalizer.Form.NFC));


    @Param({"SIMPLE", "MIXED"})
    private String type;

    private final ArrayList<String> listToSort = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private final Comparator<String> collatorComparator =
            (Comparator<String>) (Comparator<?>) Collator.getInstance(Locale.ENGLISH);

    private final Comparator<String> naturalCharStringComparator = new CharStringComparator(Character::compare);

    private final Comparator<String> defaultCharStringComparator =
            new CharStringComparator(new DefaultCharComparator());


    @Setup(Level.Invocation)
    public void prepareList() {
        ImmutableList<String> soouceStrings;
        if (type.equals("SIMPLE")) {
            soouceStrings = SIMPLE_STRINGS;
        } else if (type.equals("MIXED")) {
            soouceStrings = MIXED_STRINGS;
        } else {
            throw new IllegalArgumentException("Unknown input type: " + type);
        }
        listToSort.clear();
        listToSort.ensureCapacity(soouceStrings.size());
        listToSort.addAll(soouceStrings.asList());
    }


    @Benchmark
    public void benchmarkNaturalComparison(Blackhole blackhole) {
        Collections.sort(listToSort);
        blackhole.consume(listToSort);
    }

    @Benchmark
    public void benchmarkCollator(Blackhole blackhole) {
        Collections.sort(listToSort, collatorComparator);
        blackhole.consume(listToSort);
    }

    @Benchmark
    public void benchmarkNaturalCharStringComparator(Blackhole blackhole) {
        Collections.sort(listToSort, naturalCharStringComparator);
        blackhole.consume(listToSort);
    }

    @Benchmark
    public void benchmarkDefaultCharStringComparator(Blackhole blackhole) {
        Collections.sort(listToSort, defaultCharStringComparator);
        blackhole.consume(listToSort);
    }

}
