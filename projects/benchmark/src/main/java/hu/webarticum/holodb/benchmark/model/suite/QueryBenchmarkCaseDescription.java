package hu.webarticum.holodb.benchmark.model.suite;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class QueryBenchmarkCaseDescription {

    private final String name;

    private final String description;

    private final ImmutableList<String> initQueries;

    private final String query;

    private final int repeats;

    private final ImmutableList<QueryBenchmarkResultColumnDescription> columns;

    public QueryBenchmarkCaseDescription(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("initQueries") ImmutableList<String> initQueries,
            @JsonProperty("query") String query,
            @JsonProperty("repeats") Integer repeats,
            @JsonProperty("columns") ImmutableList<QueryBenchmarkResultColumnDescription> columns) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description != null ? description : "";
        this.initQueries = initQueries != null ? initQueries : ImmutableList.empty();
        this.query = Objects.requireNonNull(query, "query must not be null");
        this.repeats = Objects.requireNonNull(repeats, "repeats must not be null");
        this.columns = Objects.requireNonNull(columns, "columns must not be null");
    }

    @JsonGetter("name")
    public String name() {
        return name;
    }

    @JsonGetter("description")
    public String description() {
        return description;
    }

    @JsonGetter("initQueries")
    public ImmutableList<String> initQueries() {
        return initQueries;
    }

    @JsonGetter("query")
    public String query() {
        return query;
    }

    @JsonGetter("repeats")
    public int repeats() {
        return repeats;
    }

    @JsonGetter("columns")
    public ImmutableList<QueryBenchmarkResultColumnDescription> columns() {
        return columns;
    }

}
