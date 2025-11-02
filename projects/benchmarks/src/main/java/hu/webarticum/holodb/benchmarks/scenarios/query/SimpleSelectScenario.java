package hu.webarticum.holodb.benchmarks.scenarios.query;

// TODO
public class SimpleSelectScenario extends AbstractSingleQueryScenario {

    @Override
    public String name() {
        return "Simple SELECT";
    }

    @Override
    public String sql() {
        return "SELECT * FROM test_table";
    }

}
