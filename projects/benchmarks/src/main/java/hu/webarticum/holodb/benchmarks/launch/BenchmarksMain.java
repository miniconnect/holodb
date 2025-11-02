package hu.webarticum.holodb.benchmarks.launch;

import hu.webarticum.holodb.benchmarks.framework.ScenarioResult;
import hu.webarticum.holodb.benchmarks.framework.ScenarioSetResultPrinter;
import hu.webarticum.holodb.benchmarks.framework.ScenarioSetRunner;
import hu.webarticum.holodb.benchmarks.scenarios.query.ComplexSelectScenario;
import hu.webarticum.holodb.benchmarks.scenarios.query.SimpleSelectScenario;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class BenchmarksMain {

    public static void main(String[] args) {
        ImmutableList<ScenarioResult> scenarioResults = ScenarioSetRunner.builder()
                .addScenario(SimpleSelectScenario::new, 10, 100)
                .addScenario(ComplexSelectScenario::new, 10, 100)
                .build()
                .runAllScenarios();
        new ScenarioSetResultPrinter(scenarioResults).print(System.out);
    }
    
}
