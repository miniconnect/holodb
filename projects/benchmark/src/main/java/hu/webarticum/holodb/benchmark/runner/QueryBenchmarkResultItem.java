package hu.webarticum.holodb.benchmark.runner;

public class QueryBenchmarkResultItem {

    private final long executeNanos;

    private final long collectNanos;

    private QueryBenchmarkResultItem(long executeNanos, long collectNanos) {
        this.executeNanos = executeNanos;
        this.collectNanos = collectNanos;
    }

    public static QueryBenchmarkResultItem of(long executeNanos, long collectNanos) {
        return new QueryBenchmarkResultItem(executeNanos, collectNanos);
    }

    public long executeNanos() {
        return executeNanos;
    }

    public long collectNanos() {
        return collectNanos;
    }

}
