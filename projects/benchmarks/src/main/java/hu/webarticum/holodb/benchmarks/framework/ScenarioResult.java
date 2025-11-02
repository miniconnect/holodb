package hu.webarticum.holodb.benchmarks.framework;

import java.util.Optional;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class ScenarioResult {

    private final String name;
    
    private final String description;

    private final Optional<Exception> exception;

    private final ImmutableList<Long> warmupNanos;

    private final ImmutableList<Long> measurementNanos;
    
    public ScenarioResult(
            String name,
            String description,
            Optional<Exception> exception,
            ImmutableList<Long> warmupNanos,
            ImmutableList<Long> measurementNanos) {
        this.name = name;
        this.description = description;
        this.exception = exception;
        this.warmupNanos = warmupNanos;
        this.measurementNanos = measurementNanos;
    }
    
    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Optional<Exception> exception() {
        return exception;
    }

    public ImmutableList<Long> warmupNanos() {
        return warmupNanos;
    }

    public ImmutableList<Long> measurementNanos() {
        return measurementNanos;
    }
    
}
