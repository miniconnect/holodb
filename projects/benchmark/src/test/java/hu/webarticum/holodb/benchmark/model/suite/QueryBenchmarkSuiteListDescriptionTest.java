package hu.webarticum.holodb.benchmark.model.suite;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.benchmark.model.AbstractResourceBasedTest;

class QueryBenchmarkSuiteListDescriptionTest extends AbstractResourceBasedTest {

    private final static String SUITE_LIST_RESOURCE = "hu/webarticum/holodb/benchmark/sample/suite-list.yaml";

    @Test
    void testMapping() throws IOException {
        QueryBenchmarkSuiteListDescription suiteList = loadSuiteList();
        assertThat(suiteList.suites()).containsExactly("suite.yaml");
    }

    private QueryBenchmarkSuiteListDescription loadSuiteList() throws IOException {
        return loadYaml(SUITE_LIST_RESOURCE, "suite list resource stream", QueryBenchmarkSuiteListDescription.class);
    }

}
