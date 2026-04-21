package hu.webarticum.holodb.benchmark.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import hu.webarticum.holodb.benchmark.matcher.ColumnHeaderMatcher;
import hu.webarticum.holodb.benchmark.matcher.DefaultTableHeaderMatcher;
import hu.webarticum.holodb.benchmark.matcher.MatchFailedException;
import hu.webarticum.holodb.benchmark.matcher.TableHeaderMatcher;
import hu.webarticum.holodb.benchmark.model.suite.QueryBenchmarkCaseDescription;
import hu.webarticum.holodb.benchmark.model.suite.QueryBenchmarkResultColumnDescription;
import hu.webarticum.holodb.benchmark.model.suite.QueryBenchmarkSuiteDescription;
import hu.webarticum.holodb.benchmark.model.suite.QueryBenchmarkSuiteListDescription;
import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.EngineBuilder;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.api.MiniResult;
import hu.webarticum.miniconnect.api.MiniResultSet;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.jackson.JacksonSupport;
import hu.webarticum.miniconnect.record.ResultRecord;
import hu.webarticum.miniconnect.record.ResultTable;
import hu.webarticum.miniconnect.record.type.StandardValueType;

public class QueryBenchmarkController {

    private final String suiteListResourcePath;

    private QueryBenchmarkController(String suiteListResourcePath) {
        this.suiteListResourcePath = suiteListResourcePath;
    }

    public static QueryBenchmarkController ofResource(String suiteListResourcePath) {
        return new QueryBenchmarkController(suiteListResourcePath);
    }

    public void runSuites(QueryBenchmarkCaseCallback callback) {
        try {
			runSuitesInternal(callback);
		} catch (IOException e) {
		    throw new UncheckedIOException("Unexpected " + e.getClass().getSimpleName(), e);
		}
    }

    private void runSuitesInternal(QueryBenchmarkCaseCallback callback) throws IOException {
        QueryBenchmarkSuiteListDescription suiteListDescription = loadYaml(suiteListResourcePath, QueryBenchmarkSuiteListDescription.class);
        int suiteNo = 0;
        for (String relativePath  : suiteListDescription.suites()) {
            String suiteResourcePath = subpath(dirname(suiteListResourcePath), relativePath);
            handleSuite(suiteNo, suiteResourcePath, callback);
            suiteNo++;
        }
    }

    private void handleSuite(int suiteNo, String suiteResourcePath, QueryBenchmarkCaseCallback callback) throws IOException {
        QueryBenchmarkSuiteDescription suiteDescription = loadYaml(suiteResourcePath, QueryBenchmarkSuiteDescription.class);
        int caseNo = 0;
        for (QueryBenchmarkCaseDescription benchmarkCase : suiteDescription.cases()) {
            handleCase(suiteNo, caseNo, suiteResourcePath, suiteDescription, benchmarkCase, callback);
            caseNo++;
        }
    }

    private void handleCase(
            int suiteNo,
            int caseNo,
            String suiteResourcePath,
            QueryBenchmarkSuiteDescription suiteDescription,
            QueryBenchmarkCaseDescription benchmarkCase,
            QueryBenchmarkCaseCallback callback
            ) throws IOException {
        String caseName = benchmarkCase.name();
        int repeats = benchmarkCase.repeats();
        TableHeaderMatcher tableHeaderMatcher = buildTableHeaderMatcher(benchmarkCase);
        ImmutableList<MiniColumnHeader> givenColumnHeaders = executeCase(suiteResourcePath, suiteDescription, benchmarkCase);
        int warmupCount = calculateWarmupCount(suiteNo, caseNo, repeats);
        for (int i = 0; i < warmupCount; i++) {
            measureCase(suiteResourcePath, suiteDescription, benchmarkCase);
        }
        List<QueryBenchmarkResultItem> benchmarkResultItemsBuilder = new ArrayList<>();
        for (int i = 0; i < repeats; i++) {
            QueryBenchmarkResultItem benchmarkResultItem = measureCase(suiteResourcePath, suiteDescription, benchmarkCase);
            benchmarkResultItemsBuilder.add(benchmarkResultItem);
        }
        ImmutableList<QueryBenchmarkResultItem> benchmarkResultItems = ImmutableList.fromCollection(benchmarkResultItemsBuilder);
        QueryBenchmarkResult benchmarkResult = QueryBenchmarkResult.of(benchmarkResultItems);
        callback.accept(suiteResourcePath, caseName, tableHeaderMatcher, givenColumnHeaders, benchmarkResult);
    }

    private TableHeaderMatcher buildTableHeaderMatcher(QueryBenchmarkCaseDescription benchmarkCase) {
        return DefaultTableHeaderMatcher.of(benchmarkCase.columns().map(this::buildColumnHeaderMatcher));
    }

    private ColumnHeaderMatcher buildColumnHeaderMatcher(QueryBenchmarkResultColumnDescription columnDescription) {
        String name = columnDescription.name().orElse(null);
        Class<?> type = columnDescription.type();
        Boolean nullable = columnDescription.nullable().orElse(null);
        return new ConfiguredColumnHeaderMatcher(name, type, nullable);
    }

    private ImmutableList<MiniColumnHeader> executeCase(
            String suiteResourcePath,
            QueryBenchmarkSuiteDescription suiteDescription,
            QueryBenchmarkCaseDescription benchmarkCase) throws IOException {
        try (MiniSession session = loadSession(suiteResourcePath, suiteDescription)) {
            for (String query : suiteDescription.initQueries()) {
                session.execute(query).requireSuccess();
            }
            for (String query : benchmarkCase.initQueries()) {
                session.execute(query).requireSuccess();
            }
            return session.execute(benchmarkCase.query()).requireSuccess().resultSet().columnHeaders();
        }
    }

    private int calculateWarmupCount(int suiteNo, int caseNo, int repeats) {
        int suiteWarmupFactor = Math.max(1, 2 - suiteNo);
        int caseWarmupFactor = Math.max(1, 2 * (3 - caseNo));
        return suiteWarmupFactor * caseWarmupFactor * repeats;
    }

    private QueryBenchmarkResultItem measureCase(
            String suiteResourcePath,
            QueryBenchmarkSuiteDescription suiteDescription,
            QueryBenchmarkCaseDescription benchmarkCase) throws IOException {
        try (MiniSession session = loadSession(suiteResourcePath, suiteDescription)) {
            for (String query : suiteDescription.initQueries()) {
                session.execute(query).requireSuccess();
            }
            for (String query : benchmarkCase.initQueries()) {
                session.execute(query).requireSuccess();
            }
            String mainQuery = benchmarkCase.query();

            long start = System.nanoTime();
            MiniResult result = session.execute(mainQuery);
            long end = System.nanoTime();
            long executeNanos = end - start;

            start = System.nanoTime();
            processResult(result);
            end = System.nanoTime();
            long collectNanos = end - start;

            return QueryBenchmarkResultItem.of(executeNanos, collectNanos);
        }
    }

    private void processResult(MiniResult result) {
        try (MiniResultSet resultSet = result.resultSet()) {
            for (ResultRecord resultRecord : new ResultTable(resultSet)) {
                SimpleBlackhole.consume(resultRecord);
            }
        }
    }

    private MiniSession loadSession(String suiteResourcePath, QueryBenchmarkSuiteDescription suiteDescription) throws IOException {
        String holoConfigResourcePath = subpath(dirname(suiteResourcePath), suiteDescription.holoConfigResource());
        ConfigLoader configLoader = new ConfigLoader(holoConfigResourcePath);
        Engine engine = EngineBuilder.ofConfig(configLoader.load()).build();
        return new FrameworkSessionManager(engine).openSession();
    }

    private <T> T loadYaml(String resourcePath, Class<T> type) throws IOException {
        ObjectMapper mapper = JsonMapper.builder(new YAMLFactory())
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .addModule(JacksonSupport.createModule())
                .build();
        try (InputStream in = openResourceInputStream(resourcePath)) {
            return mapper.readValue(in, type);
        }
    }

    private InputStream openResourceInputStream(String resourcePath) throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("YAML resource stream is null: " + resourcePath);
        }
        return in;
    }

    private String dirname(String path) {
        int pos = path.lastIndexOf('/');
        return pos < 0 ? "" : path.substring(0, pos);
    }

    private String subpath(String parent, String sub) {
        return parent.isEmpty() ? sub : parent + "/" + sub;
    }

    private static final class ConfiguredColumnHeaderMatcher implements ColumnHeaderMatcher {

        private final String name;

        private final Class<?> type;

        private final Boolean nullable;

        ConfiguredColumnHeaderMatcher(String name, Class<?> type, Boolean nullable) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
        }

        @Override
        public boolean isMatching(MiniColumnHeader columnHeader) {
            return
                    (name == null || columnHeader.name().equals(name)) &&
                    extractType(columnHeader) == type &&
                    (nullable == null || columnHeader.isNullable() == nullable);
        }

        @Override
        public void match(MiniColumnHeader columnHeader) {
            if (name != null && !columnHeader.name().equals(name)) {
                throw new MatchFailedException("column name: " + columnHeader.name() + " != " + name);
            }
            Class<?> givenType = extractType(columnHeader);
            if (givenType != type) {
                throw new MatchFailedException("column type: " + givenType + " != " + type);
            }
            if (nullable != null && columnHeader.isNullable() != nullable) {
                throw new MatchFailedException("column nullable: " + columnHeader.isNullable() + " != " + nullable);
            }
        }

        private Class<?> extractType(MiniColumnHeader columnHeader) {
            return StandardValueType.valueOf(columnHeader.valueDefinition().type()).clazz();
        }

    }

}
