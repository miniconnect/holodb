package hu.webarticum.holodb.admin.materialize;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
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

public class Materializer {

    public static final BiFunction<String, ImmutableList<String>, String> DEFAULT_INDEX_NAMER = Materializer::createDefaultIndexName;

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StorageAccess storageAccess;
    private final Optional<String> sourceSchemaName;
    private final Predicate<String> tableFilter;
    private final Predicate<String> columnFilter;
    private final UnaryOperator<String> tableRenamer;
    private final UnaryOperator<String> columnRenamer;
    private final BiFunction<String, ImmutableList<String>, String> indexNamer;
    private final boolean dropTables;
    private final boolean createTables;
    private final boolean insertData ;

    private final DSLContext dslContext;

    private Materializer(MaterializerBuilder builder) {
        this.storageAccess = builder.storageAccess;
        this.sourceSchemaName = builder.sourceSchemaName;
        this.tableFilter = builder.tableFilter;
        this.columnFilter = builder.columnFilter;
        this.tableRenamer = builder.tableRenamer;
        this.columnRenamer = builder.columnRenamer;
        this.indexNamer = builder.indexNamer;
        this.dropTables = builder.dropTables;
        this.createTables = builder.createTables;
        this.insertData = builder.insertData;
        this.dslContext = DSL.using(builder.connection);
    }

    public static MaterializerBuilder builder(StorageAccess storageAccess, Connection connection) {
        return new MaterializerBuilder(storageAccess, connection);
    }

    private static String createDefaultIndexName(String tableName, ImmutableList<String> columnNames) {
        StringBuilder resultBuilder = new StringBuilder("idx_" + tableName);
        columnNames.forEach(v -> resultBuilder.append("_" + v));
        return resultBuilder.toString();
    }

    public void materialize() {
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
        var columns = extractColumns(table);
        if (dropTables) {
             dropTable(targetName);
        }
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
        executeQuery(dslContext.dropTableIfExists(targetName));
    }

    private void createTable(LinkedHashMap<String, ColumnItem> columns, String targetName) {
        logger.info("Create table: " + targetName);
        var fields = columns.values().stream().map(c -> c.targetField()).collect(Collectors.toList());
        executeQuery(dslContext.createTable(DSL.name(targetName)).columns(fields));
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
            executeQuery(dslContext.insertInto(targetTable, fields).values(values.asList()));
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
        executeQuery(createIndexStep.on(targetTable, indexFields));
    }

    private void executeQuery(Query query) {
        if (logger.isDebugEnabled()) {
            logger.debug("SQL: " + query.getSQL());
        }
        query.execute();
    }

    private static record ColumnItem(Column sourceColumn, Field<?> targetField) {}

    public static class MaterializerBuilder {

        private final StorageAccess storageAccess;
        private final Connection connection;

        private Optional<String> sourceSchemaName = Optional.empty();
        private Predicate<String> tableFilter = t -> true;
        private Predicate<String> columnFilter = t -> true;
        private UnaryOperator<String> tableRenamer = t -> t;
        private UnaryOperator<String> columnRenamer = t -> t;
        private BiFunction<String, ImmutableList<String>, String> indexNamer = DEFAULT_INDEX_NAMER;
        private boolean dropTables = true;
        private boolean createTables = true;
        private boolean insertData = true;

        private MaterializerBuilder(StorageAccess storageAccess, Connection connection) {
            this.storageAccess = storageAccess;
            this.connection = connection;
        }

        public MaterializerBuilder sourceSchemaName(String sourceSchemaName) {
            this.sourceSchemaName = Optional.ofNullable(sourceSchemaName);
            return this;
        }

        public MaterializerBuilder tableFilter(Predicate<String> tableFilter) {
            this.tableFilter = tableFilter;
            return this;
        }

        public MaterializerBuilder columnFilter(Predicate<String> columnFilter) {
            this.columnFilter = columnFilter;
            return this;
        }

        public MaterializerBuilder tableRenamer(UnaryOperator<String> tableRenamer) {
            this.tableRenamer = tableRenamer;
            return this;
        }

        public MaterializerBuilder columnRenamer(UnaryOperator<String> columnRenamer) {
            this.columnRenamer = columnRenamer;
            return this;
        }

        public MaterializerBuilder indexNamer(
                BiFunction<String, ImmutableList<String>, String> indexNamer) {
            this.indexNamer = indexNamer;
            return this;
        }

        public MaterializerBuilder dropTables(boolean dropTables) {
            this.dropTables = dropTables;
            return this;
        }

        public MaterializerBuilder createTables(boolean createTables) {
            this.createTables = createTables;
            return this;
        }

        public MaterializerBuilder insertData(boolean insertData) {
            this.insertData = insertData;
            return this;
        }

        public Materializer build() {
            return new Materializer(this);
        }

    }

}
