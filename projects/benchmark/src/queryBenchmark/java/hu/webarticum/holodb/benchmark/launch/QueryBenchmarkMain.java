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

    private static final int[] COLUMN_WIDTHS = { 25, 20, 7, 7, 12, 12 };

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
        if (!isQuiet) {
            printHeader();
        }
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

    private void printHeader() {
        int w0 = COLUMN_WIDTHS[0];
        int w1 = COLUMN_WIDTHS[1];
        int w2 = COLUMN_WIDTHS[2];
        int w3 = COLUMN_WIDTHS[3];
        int w4 = COLUMN_WIDTHS[4];
        int w5 = COLUMN_WIDTHS[5];
        String formatString = "| %-" + w0 + "s | %-" + w1 + "s | %-" + w2 + "s | %-" + w3 + "s | %-" + w4 + "s | %-" + w5 + "s |";
        System.out.println(String.format(
                formatString, "suite", "case", "status", "repeats", "exec avg", "collect avg"));
        for (int columnWidth : COLUMN_WIDTHS) {
            System.out.print("| " + repeat('-', columnWidth) + " ");
        }
        System.out.println("|");
    }

    private String repeat(char c, int count) {
        StringBuilder resultBuilder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            resultBuilder.append(c);
        }
        return resultBuilder.toString();
    }

    private void printResultRow(String resourcePath, String caseName, boolean success, QueryBenchmarkResult benchmarkResult) {
        String successText = success ? "SUCCESS" : "FAIL";
        int count = benchmarkResult.count();
        long executeAvg = benchmarkResult.executeNanosAvg();
        long collectAvg = benchmarkResult.collectNanosAvg();
        int w0 = COLUMN_WIDTHS[0];
        int w1 = COLUMN_WIDTHS[1];
        int w2 = COLUMN_WIDTHS[2];
        int w3 = COLUMN_WIDTHS[3];
        int w4 = COLUMN_WIDTHS[4];
        int w5 = COLUMN_WIDTHS[5];
        String formatString = "| %-" + w0 + "s | %-" + w1 + "s | %-" + w2 + "s | %" + w3 + "d | %" + w4 + "d | %" + w5 + "d |";
        System.out.println(String.format(
                formatString, basename(resourcePath), caseName, successText, count, executeAvg, collectAvg));
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
