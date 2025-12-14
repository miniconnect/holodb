package hu.webarticum.holodb.benchmarks.framework;

import java.io.IOException;
import java.util.function.Function;

import hu.webarticum.miniconnect.lang.ImmutableList;

public class ScenarioSetResultPrinter {

    private final ImmutableList<ScenarioResult> scenarioResults;

    private final ImmutableList<Column> columns = ImmutableList.of(
            new Column("name", false, ScenarioResult::name),
            new Column("state", true, r -> r.exception().isPresent() ? "exception" : "success"),
            new Column("warmups", true, r -> r.warmupNanos().size() + ""),
            new Column("measurements", true, r -> r.measurementNanos().size() + ""),
            new Column("best", true, r -> toMsString(min(r.measurementNanos()))),
            new Column("avg", true, r -> toMsString(avg(r.measurementNanos()))));

    public ScenarioSetResultPrinter(ImmutableList<ScenarioResult> scenarioResults) {
        this.scenarioResults = scenarioResults;
    }

    public void print(Appendable out) {
        try {
            printThrowing(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printThrowing(Appendable out) throws IOException {
        String[][] tableContent = getTableContent();
        int countOfScenarios = scenarioResults.size();
        int countOfColumns = columns.size();
        int[] widths = new int[countOfColumns];
        for (int c = 0; c < countOfColumns; c++) {
            for (int i = 0; i <= countOfScenarios; i++) {
                int valueWidth = tableContent[i][c].length();
                if (valueWidth > widths[c]) {
                    widths[c] = valueWidth;
                }
            }
        }
        out.append("\n");
        for (int i = 0; i <= countOfScenarios; i++) {
            for (int c = 0; c < countOfColumns; c++) {
                if (c > 0) {
                    out.append("     ");
                }
                boolean rightAligned = columns.get(c).rightAligned;
                String format = "%" + (rightAligned ? "" : "-") + widths[c] + "s";
                out.append(String.format(format, tableContent[i][c]));
            }
            out.append("\n");
        }
        out.append("\n");
    }

    private String[][] getTableContent() {
        int countOfScenarios = scenarioResults.size();
        int countOfColumns = columns.size();
        String[][] result = new String[countOfScenarios + 1][countOfColumns];
        for (int c = 0; c < countOfColumns; c++) {
            Column column = columns.get(c);
            result[0][c] = column.title;
        }
        for (int i = 0; i < countOfScenarios; i++) {
            ScenarioResult scenarioResult = scenarioResults.get(i);
            for (int c = 0; c < countOfColumns; c++) {
                Column column = columns.get(c);
                result[i + 1][c] = column.stringifier.apply(scenarioResult);
            }
        }
        return result;
    }

    private static String toMsString(long nanos) {
        long millis = nanos / 1_000_000;
        long fractions = (nanos / 1000) % 1000;
        return String.format("%d.%03d", millis, fractions);
    }

    private static long min(ImmutableList<Long> values) {
        int size = values.size();
        if (size == 0) {
            return -1;
        }

        long min = values.get(0);
        for (int i = 1; i < size; i++) {
            long value = values.get(i);
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    private static long avg(ImmutableList<Long> values) {
        int size = values.size();
        if (size == 0) {
            return -1;
        }

        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / size;
    }

    private static class Column {

        final String title;

        final boolean rightAligned;

        final Function<ScenarioResult, String> stringifier;

        Column(String title, boolean rightAligned, Function<ScenarioResult, String> stringifier) {
            this.title = title;
            this.rightAligned = rightAligned;
            this.stringifier = stringifier;
        }

    }

}
