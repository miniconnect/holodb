package hu.webarticum.holodb.benchmark.model.suite;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryBenchmarkSuiteListDescription {

    private final ImmutableList<String> suites;

    public QueryBenchmarkSuiteListDescription(
            @JsonProperty("suites") ImmutableList<String> suites) {
        this.suites = suites != null ? suites : ImmutableList.empty();
    }

    @JsonGetter("suites")
    public ImmutableList<String> suites() {
        return suites;
    }

}
