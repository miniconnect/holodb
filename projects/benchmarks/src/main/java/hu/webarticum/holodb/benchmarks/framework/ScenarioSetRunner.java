package hu.webarticum.holodb.benchmarks.framework;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class ScenarioSetRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioSetRunner.class);

    private final ImmutableList<ScenarioRunner> scenarioRunners;

    private ScenarioSetRunner(Builder builder) {
        this.scenarioRunners = ImmutableList.fromCollection(builder.scenarioRunners);
    }

    public static Builder builder() {
        return new Builder();
    }

    public ImmutableList<ScenarioResult> runAllScenarios() {
        int numberOfScenarios = scenarioRunners.size();
        logger.info("Start executing {} scenarios", numberOfScenarios);
        ImmutableList<ScenarioResult> result = scenarioRunners.map(r -> r.runScenario());
        logger.info("{} scenarios completed", numberOfScenarios);
        return result;
    }

    public static class Builder {

        private final List<ScenarioRunner> scenarioRunners = new LinkedList<>();

        private Builder() {
            // use builder()
        }

        public Builder addScenario(Supplier<Scenario> scenarioFactory, int warmups, int measurements) {
            scenarioRunners.add(new ScenarioRunner(scenarioFactory, warmups, measurements));
            return this;
        }

        public ScenarioSetRunner build() {
            return new ScenarioSetRunner(this);
        }

    }

}
