package hu.webarticum.holodb.benchmark.model.suite;

import static org.assertj.core.api.Assertions.assertThat;
import static hu.webarticum.miniconnect.lang.assertj.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.benchmark.model.AbstractResourceBasedTest;

class QueryBenchmarkSuiteDescriptionTest extends AbstractResourceBasedTest {

    private final static String SUITE_RESOURCE = "hu/webarticum/holodb/benchmark/sample/suite.yaml";

    @Test
    void testMappingOfSuite() throws IOException {
        QueryBenchmarkSuiteDescription suite = loadSuite();
        assertThat(suite.description()).isEqualTo("This is a sample benchmark suite");
        assertThat(suite.holoConfigResource()).isEqualTo("dataset.yaml");
        assertThat(suite.initQueries()).containsExactly("USE sch");
        assertThat(suite.cases()).hasSize(1);

        QueryBenchmarkCaseDescription case1 = suite.cases().get(0);
        assertThat(case1.name()).isEqualTo("case-1");
        assertThat(case1.description()).isEqualTo("This is a benchmark case");
        assertThat(case1.initQueries()).isEmpty();
        assertThat(case1.query()).isEqualTo("SELECT id FROM tbl ORDER BY id LIMIT 1");
        assertThat(case1.columns()).hasSize(1);
        assertThat(case1.columns().get(0).name()).hasValue("code");
        assertThat(case1.columns().get(0).type()).isEqualTo(String.class);
        assertThat(case1.columns().get(0).nullable()).hasValue(false);
    }

    private QueryBenchmarkSuiteDescription loadSuite() throws IOException {
        return loadYaml(SUITE_RESOURCE, "suite 1 resource stream", QueryBenchmarkSuiteDescription.class);
    }

}
