package hu.webarticum.holodb.benchmark.launch;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import hu.webarticum.holodb.benchmark.matcher.TableHeaderMatcher;
import hu.webarticum.holodb.benchmark.runner.QueryBenchmarkController;
import hu.webarticum.holodb.benchmark.runner.QueryBenchmarkResult;
import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.lang.ImmutableList;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "QueryTestMain", mixinStandardHelpOptions = true)
public class QueryBenchmarkMain implements Callable<Integer> {

    @Parameters(
            paramLabel = "<test-suite-list-resource>",
            description = "Resource path to a file containing the list of test suites.",
            arity = "1")
    private String testSuiteListResourcePath;

    @Option(
            names = "--quiet",
            paramLabel = "<quiet>",
            description = "Enables quiet mode that prints no output.",
            arity = "0..1",
            defaultValue = "false")
    private boolean isQuiet;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryBenchmarkMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        AtomicInteger totalCounter = new AtomicInteger(0);
        AtomicInteger successCounter = new AtomicInteger(0);
        QueryBenchmarkController
                .ofResource(testSuiteListResourcePath)
                .runSuites((path, name, matcher, headers, result) -> {
                    boolean success = acceptCase(path, name, matcher, headers, result);
                    totalCounter.incrementAndGet();
                    if (success) {
                        successCounter.incrementAndGet();
                    }
                });
        int totalCount = totalCounter.get();
        int successCount = successCounter.get();
        if (!isQuiet) {
            System.out.println();
            System.out.println(String.format(
                    "%2$s/%1$s benchmark was run successfully",
                    totalCount, successCount));
        }
        return totalCount == successCount ? 0 : 1;
    }

    private boolean acceptCase(
            String resourcePath,
            String caseName,
            TableHeaderMatcher tableHeaderMatcher,
            ImmutableList<MiniColumnHeader> givenColumnHeaders,
            QueryBenchmarkResult benchmarkResult) {
        boolean success = true;
        try {
            tableHeaderMatcher.match(givenColumnHeaders);
        } catch (Exception e) {
            success = false;
        }
        if (!isQuiet) {
            printResultRow(resourcePath, caseName, success, benchmarkResult);
        }
        return success;
    }

    private void printResultRow(String resourcePath, String caseName, boolean success, QueryBenchmarkResult benchmarkResult) {
        String successText = success ? "SUCCESS" : "FAIL";
        int count = benchmarkResult.count();
        long executeAvg = benchmarkResult.executeNanosAvg();
        long collectAvg = benchmarkResult.collectNanosAvg();
        System.out.println(String.format(
                "| %-25s | %-20s | %-7s | %7d | %9d | %9d |",
                basename(resourcePath), caseName, successText, count, executeAvg, collectAvg));
    }

    private String basename(String path) {
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            return path;
        } else {
            return path.substring(pos + 1);
        }
    }

}
