package hu.webarticum.holodb.benchmark.model.suite;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryBenchmarkResultColumnDescription {

    private final String name;

    private final Class<?> type;

    private final Boolean nullable;

    public QueryBenchmarkResultColumnDescription(
            @JsonProperty("name") String name,
            @JsonProperty("type") Class<?> type,
            @JsonProperty("nullable") Boolean nullable) {
        this.name = name;
        this.type = Objects.requireNonNull(type);
        this.nullable = nullable;
    }

    @JsonGetter("name")
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    @JsonGetter("type")
    public Class<?> type() {
        return type;
    }

    @JsonGetter("nullable")
    public Optional<Boolean> nullable() {
        return Optional.ofNullable(nullable);
    }

}
