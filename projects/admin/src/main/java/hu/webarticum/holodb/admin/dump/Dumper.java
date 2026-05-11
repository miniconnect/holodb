package hu.webarticum.holodb.admin.dump;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jooq.DataType;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.holodb.admin.util.JooqUtil;
import hu.webarticum.minibase.storage.api.Column;
import hu.webarticum.minibase.storage.api.ColumnDefinition;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Row;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class Dumper {

    public static final BiFunction<String, ImmutableList<String>, String> DEFAULT_INDEX_NAMER = Dumper::createDefaultIndexName;

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StorageAccess storageAccess;
    private final DSLContext dslContext;
    private final Optional<String> sourceSchemaName;
    private final Predicate<String> tableFilter;
    private final Predicate<String> columnFilter;
    private final UnaryOperator<String> tableRenamer;
    private final UnaryOperator<String> columnRenamer;
    private final BiFunction<String, ImmutableList<String>, String> indexNamer;
    private final boolean dropTables;
    private final boolean createTables;
    private final boolean insertData ;
    private final Consumer<Query> queryCallback;

    private Dumper(DumperBuilder builder, Consumer<Query> queryCallback) {
        this.storageAccess = builder.storageAccess;
        this.dslContext = builder.dslContext;
        this.sourceSchemaName = builder.sourceSchemaName;
        this.tableFilter = builder.tableFilter;
        this.columnFilter = builder.columnFilter;
        this.tableRenamer = builder.tableRenamer;
        this.columnRenamer = builder.columnRenamer;
        this.indexNamer = builder.indexNamer;
        this.dropTables = builder.dropTables;
        this.createTables = builder.createTables;
        this.insertData = builder.insertData;
        this.queryCallback = queryCallback;
    }

    public static DumperBuilder builder(StorageAccess storageAccess, DSLContext dslContext) {
        return new DumperBuilder(storageAccess, dslContext);
    }

    private static String createDefaultIndexName(String tableName, ImmutableList<String> columnNames) {
        StringBuilder resultBuilder = new StringBuilder("idx_" + tableName);
        columnNames.forEach(v -> resultBuilder.append("_" + v));
        return resultBuilder.toString();
    }

    public void dump() {
        for (Table table : findSchema().tables().resources()) {
            if (isTableIncluded(table)) {
                materializeTable(table);
            }
        }
    }

    private Schema findSchema() {
        NamedResourceStore<Schema> schemaStore = storageAccess.schemas();
        if (sourceSchemaName.isPresent()) {
            String name = sourceSchemaName.get();
            Schema schema = schemaStore.get(name);
            if (schema == null) {
                throw new IllegalArgumentException("Source schema not found: " + name);
            }
            return schema;
        }
        ImmutableList<String> names = schemaStore.names();
        if (names.isEmpty()) {
            throw new IllegalArgumentException("No source schema found");
        }
        return schemaStore.get(names.get(0));
    }

    private boolean isTableIncluded(Table table) {
        return tableFilter.test(table.name());
    }

    private void materializeTable(Table table) {
        String targetName = tableRenamer.apply(table.name());
        if (dropTables) {
            dropTable(targetName);
        }
        var columns = extractColumns(table);
        if (columns.isEmpty()) {
            logger.info("Skipped table with no columns: " + targetName);
            return;
        }

        if (createTables) {
            createTable(columns, targetName);
        }
        if (insertData) {
            populateTable(table, columns, targetName);
        }
        if (createTables) {
            postprocessTable(table, columns, targetName);
        }
    }

    private LinkedHashMap<String, ColumnItem> extractColumns(Table table) {
        LinkedHashMap<String, ColumnItem> result = new LinkedHashMap<>();
        String tableName = table.name();
        for (Column column : table.columns().resources()) {
            String columnName = column.name();
            String fullyQualifiedName = tableName + "." + columnName;
            if (columnFilter.test(fullyQualifiedName)) {
                String targetName = columnRenamer.apply(column.name());
                Field<?> field = DSL.field(DSL.name(targetName), dataTypeOf(column));
                result.put(columnName, new ColumnItem(column, field));
            }
        }
        return result;
    }

    private DataType<?> dataTypeOf(Column column) {
        ColumnDefinition columnDefinition = column.definition();
        DataType<?> dataType = JooqUtil.toDataType(column).nullable(columnDefinition.isNullable());
        if (columnDefinition.isAutoIncremented()) {
            dataType = dataType.generatedByDefaultAsIdentity();
        }
        Object defaultValue = columnDefinition.defaultValue();
        if (defaultValue != null && !columnDefinition.isAutoIncremented()) {
            dataType = applyDefaultValue(dataType, JooqUtil.normalizeValue(defaultValue));
        }
        return dataType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DataType<?> applyDefaultValue(DataType<?> dataType, Object defaultValue) {
        return ((DataType) dataType).defaultValue(defaultValue);
    }

    private void dropTable(String targetName) {
        logger.info("Drop table: " + targetName);
        applyQuery(dslContext.dropTableIfExists(targetName));
    }

    private void createTable(LinkedHashMap<String, ColumnItem> columns, String targetName) {
        logger.info("Create table: " + targetName);
        var fields = columns.values().stream().map(c -> c.targetField()).collect(Collectors.toList());
        applyQuery(dslContext.createTable(DSL.name(targetName)).columns(fields));
    }

    private void populateTable(Table table, LinkedHashMap<String, ColumnItem> columns, String targetName) {
        logger.info("Populate table: " + targetName);
        var targetTable = DSL.table(DSL.name(targetName));
        LargeInteger rowCount = table.size();
        var fields = columns.values().stream().map(c -> c.targetField()).collect(Collectors.toList());
        var sourceColumnNames = ImmutableList.fromCollection(columns.keySet());
        for (var i = LargeInteger.ZERO; i.isLessThan(rowCount); i = i.increment()) {
            Row row = table.row(i);
            var values = sourceColumnNames.map(k -> JooqUtil.normalizeValue(row.get(k)));
            applyQuery(dslContext.insertInto(targetTable, fields).values(values.asList()));
        }
    }

    private void postprocessTable(Table table, LinkedHashMap<String, ColumnItem> columns, String targetName) {
        logger.info("Postprocess table: " + targetName);
        for (TableIndex index : table.indexes().resources()) {
            addIndexIfApplicable(targetName, index, columns);
        }
    }

    private void addIndexIfApplicable(String targetTableName, TableIndex index, LinkedHashMap<String, ColumnItem> columns) {
        var allColumnNames = index.columnNames();
        List<Field<?>> indexFields = new ArrayList<>(allColumnNames.size());
        for (String columnName : allColumnNames) {
            ColumnItem columnItem = columns.get(columnName);
            if (columnItem == null) {
                return;
            }
            indexFields.add(columnItem.targetField());
        }
        ImmutableList<String> targetColumnNames = indexFields.stream()
                .map(Field::getName).collect(ImmutableList.createCollector());
        String indexName = indexNamer.apply(targetTableName, targetColumnNames);
        var createIndexStep = index.isUnique() ?
                dslContext.createUniqueIndex(DSL.name(indexName)) :
                dslContext.createIndex(DSL.name(indexName));
        var targetTable = DSL.table(DSL.name(targetTableName));
        applyQuery(createIndexStep.on(targetTable, indexFields));
    }

    private void applyQuery(Query query) {
        queryCallback.accept(query);
    }

    private static record ColumnItem(Column sourceColumn, Field<?> targetField) {}

    public static class DumperBuilder {

        private final StorageAccess storageAccess;
        private final DSLContext dslContext;

        private Optional<String> sourceSchemaName = Optional.empty();
        private Predicate<String> tableFilter = t -> true;
        private Predicate<String> columnFilter = t -> true;
        private UnaryOperator<String> tableRenamer = t -> t;
        private UnaryOperator<String> columnRenamer = t -> t;
        private BiFunction<String, ImmutableList<String>, String> indexNamer = DEFAULT_INDEX_NAMER;
        private boolean dropTables = true;
        private boolean createTables = true;
        private boolean insertData = true;

        private DumperBuilder(StorageAccess storageAccess, DSLContext dslContext) {
            this.storageAccess = storageAccess;
            this.dslContext = dslContext;
        }

        public DumperBuilder sourceSchemaName(String sourceSchemaName) {
            this.sourceSchemaName = Optional.ofNullable(sourceSchemaName);
            return this;
        }

        public DumperBuilder tableFilter(Predicate<String> tableFilter) {
            this.tableFilter = tableFilter;
            return this;
        }

        public DumperBuilder columnFilter(Predicate<String> columnFilter) {
            this.columnFilter = columnFilter;
            return this;
        }

        public DumperBuilder tableRenamer(UnaryOperator<String> tableRenamer) {
            this.tableRenamer = tableRenamer;
            return this;
        }

        public DumperBuilder columnRenamer(UnaryOperator<String> columnRenamer) {
            this.columnRenamer = columnRenamer;
            return this;
        }

        public DumperBuilder indexNamer(
                BiFunction<String, ImmutableList<String>, String> indexNamer) {
            this.indexNamer = indexNamer;
            return this;
        }

        public DumperBuilder dropTables(boolean dropTables) {
            this.dropTables = dropTables;
            return this;
        }

        public DumperBuilder createTables(boolean createTables) {
            this.createTables = createTables;
            return this;
        }

        public DumperBuilder insertData(boolean insertData) {
            this.insertData = insertData;
            return this;
        }

        public Dumper build(Consumer<Query> queryCallback) {
            return new Dumper(this, queryCallback);
        }

    }

}
