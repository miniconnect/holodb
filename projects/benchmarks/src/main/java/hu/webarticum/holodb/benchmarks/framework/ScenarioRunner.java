package hu.webarticum.holodb.benchmarks.framework;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class ScenarioRunner {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioRunner.class);

    private Supplier<Scenario> scenarioFactory;

    private int warmups;

    private int measurements;

    public ScenarioRunner(Supplier<Scenario> scenarioFactory, int warmups, int measurements) {
        this.scenarioFactory = scenarioFactory;
        this.warmups = warmups;
        this.measurements = measurements;
    }

    public ScenarioResult runScenario() {
        Scenario scenarioForMetadata = scenarioFactory.get();
        String name = scenarioForMetadata.name();
        String description = scenarioForMetadata.description();
        logger.info("Start scenario '{}' ('{}') with {} warmups and {} measurements", name, description, warmups, measurements);
        List<Long> warmupNanos = new LinkedList<>();
        List<Long> measurementNanos = new LinkedList<>();
        Exception exception = null;
        try {
            for (int i = 0; i < warmups; i++) {
                logger.debug("Start warmup iteration for scenario '{}'", name);
                long nanos = runIteration();
                logger.debug("Warmup iteration for scenario '{}' fished in {} nanoseconds", name, nanos);
                warmupNanos.add(nanos);
            }
            for (int i = 0; i < measurements; i++) {
                logger.debug("Start measurement iteration for scenario '{}'", name);
                long nanos = runIteration();
                logger.debug("Measurement iteration for scenario '{}' fished in {} nanoseconds", name, nanos);
                measurementNanos.add(nanos);
            }
        } catch (Exception e) {
            logger.error("An exception occured during scenario '{}'", name, e);
            exception = e;
        }
        logger.info("Scenario '{}' completed", name);
        return new ScenarioResult(
                name,
                description,
                Optional.ofNullable(exception),
                ImmutableList.fromCollection(warmupNanos),
                ImmutableList.fromCollection(measurementNanos));
    }

    private long runIteration() throws Exception {
        Scenario scenario = scenarioFactory.get();
        long startTime;
        long endTime;
        try {
            scenario.setUp();
            startTime = System.nanoTime();
            scenario.runOnce();
            endTime = System.nanoTime();
        } finally {
            scenario.tearDown();
        }
        return endTime - startTime;
    }

}
