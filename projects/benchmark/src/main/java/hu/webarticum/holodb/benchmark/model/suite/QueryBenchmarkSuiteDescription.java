package hu.webarticum.holodb.benchmark.model.suite;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryBenchmarkSuiteDescription {

    private final String description;

    private final String holoConfigResource;

    private final ImmutableList<String> initQueries;

    private final ImmutableList<QueryBenchmarkCaseDescription> cases;

    public QueryBenchmarkSuiteDescription(
            @JsonProperty("description") String description,
            @JsonProperty("holoConfigResource") String holoConfigResource,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("cases") ImmutableList<QueryBenchmarkCaseDescription> cases) {
        this.description = description != null ? description : "";
        this.holoConfigResource = Objects.requireNonNull(holoConfigResource, "holoConfigResource must not be null");
        this.initQueries = initQueries != null ? initQueries : ImmutableList.empty();
        this.cases = Objects.requireNonNull(cases, "cases must not be null");
        Set<String> names = new HashSet<>(cases.size());
        for (QueryBenchmarkCaseDescription testCase : cases) {
            String caseName = testCase.name();
            if (!names.add(caseName)) {
                throw new IllegalArgumentException("Duplicated case name: " + caseName);
            }
        }
    }

    @JsonGetter("description")
    public String description() {
        return description;
    }

    @JsonGetter("holoConfigResource")
    public String holoConfigResource() {
        return holoConfigResource;
    }

    @JsonGetter("initQueries")
    public ImmutableList<String> initQueries() {
        return initQueries;
    }

    @JsonGetter("cases")
    public ImmutableList<QueryBenchmarkCaseDescription> cases() {
        return cases;
    }

}
