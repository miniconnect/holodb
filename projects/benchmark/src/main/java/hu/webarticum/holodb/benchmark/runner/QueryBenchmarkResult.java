package hu.webarticum.holodb.benchmark.runner;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryBenchmarkResult {

    private final ImmutableList<QueryBenchmarkResultItem> items;

    private QueryBenchmarkResult(ImmutableList<QueryBenchmarkResultItem> items) {
        this.items = items;
    }

    public static QueryBenchmarkResult of (ImmutableList<QueryBenchmarkResultItem> items) {
        return new QueryBenchmarkResult(items);
    }

    public ImmutableList<QueryBenchmarkResultItem> items() {
        return items;
    }

    public int count() {
        return items.size();
    }

    public long executeNanosAvg() {
        if (items.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (QueryBenchmarkResultItem item : items) {
            sum += item.executeNanos();
        }
        return sum / items.size();
    }

    public long collectNanosAvg() {
        if (items.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (QueryBenchmarkResultItem item : items) {
            sum += item.collectNanos();
        }
        return sum / items.size();
    }

}
