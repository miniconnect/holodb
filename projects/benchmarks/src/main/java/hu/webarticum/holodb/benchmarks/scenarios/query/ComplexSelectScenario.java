package hu.webarticum.holodb.benchmarks.scenarios.query;

// TODO
public class ComplexSelectScenario extends AbstractSingleQueryScenario {

    @Override
    public String name() {
        return "Complex SELECT";
    }

    @Override
    public String sql() {
        return "SELECT id, id + 1, code, year - 20 FROM test_table WHERE year > 1960 ORDER BY code DESC LIMIT 500";
    }

}
