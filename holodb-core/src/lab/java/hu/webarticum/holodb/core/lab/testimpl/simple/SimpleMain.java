package hu.webarticum.holodb.core.lab.testimpl.simple;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import hu.webarticum.holodb.core.data.binrel.monotonic.FastMonotonic;
import hu.webarticum.holodb.core.data.binrel.permutation.DirtyFpePermutation;
import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.selection.Selection;
import hu.webarticum.holodb.core.data.source.ArraySortedSource;
import hu.webarticum.holodb.core.data.source.Index;
import hu.webarticum.holodb.core.data.source.IndexedSource;
import hu.webarticum.holodb.core.data.source.RangeSource;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.holodb.core.model.Schema;
import hu.webarticum.holodb.core.model.Table;
import hu.webarticum.holodb.core.model.TableIndex;

public class SimpleMain {

    private static final char ROWLINE_CHAR = '-';

    private static final char ROWLINE_SEP_CHAR = '+';
    
    private static final char CELL_SEP_CHAR = '|';
    

    public static void main(String[] args) {
        Schema schema = createSchema();
        dumpSchema(schema);
        runSomeQueries(schema);
    }
    
    
    private static Schema createSchema() {
        SimpleSchema schema = new SimpleSchema();
        
        BigInteger tableSize = BigInteger.valueOf(100);
        
        SimpleTable table = new SimpleTable();
        schema.addTable("test", table);
        
        IndexedSource<BigInteger> idSource = new RangeSource(BigInteger.ONE, tableSize);
        SimpleColumn idColumn = new SimpleColumn(idSource);
        table.addColumn("id", idColumn);
        table.addIndex(idSource, "id");

        IndexedSource<String> lastNameSource = new SimpleIndexedSource<>(
                new ArraySortedSource<>(
                        "Almási", "Arató", "Bán", "Benedek", "Béni", "Csobánki",
                        "Dárdás", "Fischer", "Galambos", "Garas", "Horváth",
                        "Kenéz", "Kovács", "Levente", "Major", "Nagy",
                        "Orsós", "Pataki", "Rónai", "Rudolf",
                        "Sas", "Szabó", "Takács", "Teleki", "Varga"),
                tableSize,
                (i, s) -> new FastMonotonic(s, i),
                s -> new DirtyFpePermutation("lastname-key".getBytes(StandardCharsets.UTF_8), s));
        SimpleColumn lastNameColumn = new SimpleColumn(lastNameSource);
        table.addColumn("lastname", lastNameColumn); // NOSONAR
        table.addIndex(lastNameSource, "lastname");

        IndexedSource<String> firstNameSource = new SimpleIndexedSource<>(
                new ArraySortedSource<>(
                        "Anna", "Béla", "Csilla", "Dániel", "Dénes",
                        "Elek", "Elemér", "Emese", "Ferenc",
                        "Gabriella", "Hugó", "Ilona",
                        "Judit", "Krisztián", "Lajos",
                        "Mária", "Olga", "Péter",
                        "Sándor", "Szilvia", "Tamás", "Viktor"),
                tableSize,
                (i, s) -> new FastMonotonic(s, i),
                s -> new DirtyFpePermutation("firstname-key".getBytes(StandardCharsets.UTF_8), s));
        SimpleColumn firstNameColumn = new SimpleColumn(firstNameSource);
        table.addColumn("firstname", firstNameColumn); // NOSONAR
        table.addIndex(firstNameSource, "firstname");

        IndexedSource<Optional<String>> citySource = new SimpleNullableIndexedSource<>(
                new ArraySortedSource<>(
                        "Budapest", "Hódmezővásárhely", "Kecskemét", "Pécs", "Szeged"),
                tableSize,
                tableSize.divide(BigInteger.TWO),
                (i, s) -> new FastMonotonic(s, i),
                s -> new DirtyFpePermutation("city-key".getBytes(StandardCharsets.UTF_8), s));
        SimpleColumn cityColumn = new SimpleColumn(citySource);
        table.addColumn("city", cityColumn);
        table.addIndex(citySource, "city");
        
        return schema;
    }
    
    private static void dumpSchema(Schema schema) {
        for (String tableName : schema.tableNames()) {
            dumpTable(tableName, schema.table(tableName));
        }
    }
    
    private static void dumpTable(String tableName, Table table) {
        Map<String, Integer> columnWidths = calculateColumnWidths(table);

        printTitle(tableName);
        
        drawRowLine(table, columnWidths);
        dumpHeader(table, columnWidths);
        drawRowLine(table, columnWidths);
        drawRowLine(table, columnWidths);
        
        int size = table.size().intValue();
        for (int i = 0; i < size; i++) {
            dumpRow(table, i, columnWidths);
            drawRowLine(table, columnWidths);
        }
    }
    
    private static void dumpResults(String title, Table table, Selection selection) {
        Map<String, Integer> columnWidths = calculateColumnWidths(table);

        printTitle(title);
        
        drawRowLine(table, columnWidths);
        dumpHeader(table, columnWidths);
        drawRowLine(table, columnWidths);
        drawRowLine(table, columnWidths);
        
        int size = selection.size().intValue();
        for (int i = 0; i < size; i++) {
            dumpRow(table, selection.at(BigInteger.valueOf(i)).intValue(), columnWidths);
            drawRowLine(table, columnWidths);
        }
    }
    
    private static void printTitle(String title) {
        System.out.println(); // NOSONAR
        System.out.println(String.format("| # %s |", title)); // NOSONAR
    }
    
    private static Map<String, Integer> calculateColumnWidths(Table table) {
        Map<String, Integer> columnWidths = new HashMap<>();
        
        for (String columnName : table.columnNames()) {
            int columnWidth = columnName.length();
            int size = table.size().intValue();
            Source<?> source = table.column(columnName).source();
            for (int i = 0; i < size; i++) {
                int valueLength;
                try {
                    valueLength = fetchValueAsString(source, i).length();
                } catch (Exception e) {
                    System.out.println(source.size() + " / " + i); // NOSONAR
                    throw e;
                }
                if (valueLength > columnWidth) {
                    columnWidth = valueLength;
                }
            }
            columnWidths.put(columnName, columnWidth);
        }
        
        return columnWidths;
    }
    
    private static void dumpHeader(Table table, Map<String, Integer> columnWidths) {
        System.out.print(CELL_SEP_CHAR); // NOSONAR
        for (String columnName : table.columnNames()) {
            int columnWidth = columnWidths.get(columnName);
            dumpValue(columnName, columnWidth);
            System.out.print(CELL_SEP_CHAR); // NOSONAR
        }
        System.out.println(); // NOSONAR
    }

    private static void dumpRow(Table table, int index, Map<String, Integer> columnWidths) {
        System.out.print(CELL_SEP_CHAR); // NOSONAR
        for (String columnName : table.columnNames()) {
            Source<?> source = table.column(columnName).source();
            String value = fetchValueAsString(source, index);
            int columnWidth = columnWidths.get(columnName);
            dumpValue(value, columnWidth);
            System.out.print(CELL_SEP_CHAR); // NOSONAR
        }
        System.out.println(); // NOSONAR
    }
    
    private static void drawRowLine(Table table, Map<String, Integer> columnWidths) {
        System.out.print(ROWLINE_SEP_CHAR); // NOSONAR
        for (String columnName : table.columnNames()) {
            int columnWidth = columnWidths.get(columnName);
            int lineWidth = columnWidth + 2;
            for (int i = 0; i < lineWidth; i++) {
                System.out.print(ROWLINE_CHAR); // NOSONAR
            }
            System.out.print(ROWLINE_SEP_CHAR); // NOSONAR
        }
        System.out.println(); // NOSONAR
    }

    private static void dumpValue(Object value, int width) {
        String strValue = value.toString();
        String format = String.format(" %%%ds ", width);
        System.out.print(String.format(format, strValue)); // NOSONAR
    }

    private static String fetchValueAsString(Source<?> source, int index) {
        Object value = source.get(BigInteger.valueOf(index));
        if (value instanceof Optional) {
            value = ((Optional<?>) value).orElse(null);
        }
        return Objects.toString(value);
    }
    
    private static void runSomeQueries(Schema schema) {
        runValueQuery(schema, "test", "lastname", "Horváth");
        runRangeQuery(schema, "test", "firstname", "Helga", "Krisztián");
        runValueQuery(schema, "test", "city", Optional.of("Hódmezővásárhely"));
    }
    
    private static void runValueQuery(
            Schema schema,
            String tableName,
            String columnName,
            Object searchValue) {
        
        Table table = schema.table(tableName);
        
        Selection resultSelection = Range.empty(BigInteger.ZERO);
        for (TableIndex tableIndex : table.indices()) {
            List<String> indexColumnNames = tableIndex.columnNames();
            if (indexColumnNames.size() == 1 && indexColumnNames.get(0).contentEquals(columnName)) {
                @SuppressWarnings("unchecked")
                Index<Object> index = (Index<Object>) tableIndex.index(searchValue.getClass());
                resultSelection = index.find(searchValue);
                break;
            }
        }

        String title = String.format(
                "Search '%s' in %s.%s",
                searchValue, tableName, columnName);
        dumpResults(title, table, resultSelection);
    }

    private static void runRangeQuery(
            Schema schema,
            String tableName,
            String columnName,
            Object searchMin,
            Object searchMax) {
        
        Table table = schema.table(tableName);
        
        Selection resultSelection = Range.empty(BigInteger.ZERO);
        for (TableIndex tableIndex : table.indices()) {
            List<String> indexColumnNames = tableIndex.columnNames();
            if (indexColumnNames.size() == 1 && indexColumnNames.get(0).contentEquals(columnName)) {
                @SuppressWarnings("unchecked")
                Index<Object> index = (Index<Object>) tableIndex.index(searchMin.getClass());
                resultSelection = index.findBetween(searchMin, true, searchMax, true);
                break;
            }
        }

        String title = String.format(
                "Search '%s'..'%s' in %s.%s",
                searchMin, searchMax, tableName, columnName);
        dumpResults(title, table, resultSelection);
    }

}
