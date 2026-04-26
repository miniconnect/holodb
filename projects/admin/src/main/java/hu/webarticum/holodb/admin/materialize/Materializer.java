package hu.webarticum.holodb.admin.materialize;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.minibase.storage.api.Table;
import hu.webarticum.minibase.storage.api.TableIndex;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class Materializer {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StorageAccess storageAccess;
    private final Optional<String> sourceSchemaName;
    private final Predicate<String> tableFilter;
    private final UnaryOperator<String> tableRenamer;
    private final boolean dropTables;
    private final boolean createTables;
    private final boolean insertData ;

    private final DSLContext dslContext;

    private Materializer(MaterializerBuilder builder) {
        this.storageAccess = builder.storageAccess;
        this.sourceSchemaName = builder.sourceSchemaName;
        this.tableFilter = builder.tableFilter;
        this.tableRenamer = builder.tableRenamer;
        this.dropTables = builder.dropTables;
        this.createTables = builder.createTables;
        this.insertData = builder.insertData;
        this.dslContext = DSL.using(builder.connection);
    }

    public static MaterializerBuilder builder(StorageAccess storageAccess, Connection connection) {
        return new MaterializerBuilder(storageAccess, connection);
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
        var fields = extractFields(table);
        if (dropTables) {
             dropTable(targetName);
        }
        if (createTables) {
            createTable(fields, targetName);
        }
        if (insertData) {
            populateTable(table, fields, targetName);
        }
        if (createTables) {
            postprocessTable(table, targetName);
        }
    }

    private static List<? extends Field<?>> extractFields(Table table) {
        return table.columns().resources().map(c -> fieldOf(c)).asList();
    }

    private static Field<?> fieldOf(Column column) {
        ColumnDefinition columnDefinition = column.definition();
        DataType<?> dataType = JooqUtil.toDataType(column).nullable(columnDefinition.isNullable());
        if (columnDefinition.isAutoIncremented()) {
            dataType = dataType.generatedByDefaultAsIdentity();
        }
        Object defaultValue = columnDefinition.defaultValue();
        if (defaultValue != null && !columnDefinition.isAutoIncremented()) {
            dataType = applyDefaultValue(dataType, JooqUtil.normalizeValue(defaultValue));
        }
        return DSL.field(DSL.name(column.name()), dataType);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static DataType<?> applyDefaultValue(DataType<?> dataType, Object defaultValue) {
        return ((DataType) dataType).defaultValue(defaultValue);
    }

    private void dropTable(String targetName) {
        logger.info("Drop table: " + targetName);
        executeQuery(dslContext.dropTableIfExists(targetName));
    }

    private void createTable(List<? extends Field<?>> fields, String targetName) {
        logger.info("Create table: " + targetName);
        executeQuery(dslContext.createTable(DSL.name(targetName)).columns(fields));
    }

    private void populateTable(Table table, List<? extends Field<?>> fields, String targetName) {
        logger.info("Populate table: " + targetName);
        var targetTable = DSL.table(DSL.name(targetName));
        LargeInteger rowCount = table.size();
        for (var i = LargeInteger.ZERO; i.isLessThan(rowCount); i = i.increment()) {
            executeQuery(dslContext
                    .insertInto(targetTable, fields)
                    .values(table.row(i).getAll().map(v -> JooqUtil.normalizeValue(v)).asList()));
        }
    }

    private void postprocessTable(Table table, String targetName) {
        logger.info("Postprocess table: " + targetName);
        var targetTable = DSL.table(DSL.name(targetName));
        for (TableIndex index : table.indexes().resources()) {
            var indexName = DSL.name(targetName + "_" + index.name());
            var createIndexStep = index.isUnique() ?
                    dslContext.createUniqueIndex(indexName) :
                    dslContext.createIndex(indexName);
            executeQuery(createIndexStep.on(targetTable, indexFields(index)));
        }
    }

    private List<? extends Field<?>> indexFields(TableIndex index) {
        return index.columnNames().map(n -> DSL.field(DSL.name(n))).asList();
    }

    private void executeQuery(Query query) {
        if (logger.isDebugEnabled()) {
            logger.debug("SQL: " + query.getSQL());
        }
        query.execute();
    }

    public static class MaterializerBuilder {

        private final StorageAccess storageAccess;
        private final Connection connection;

        private Optional<String> sourceSchemaName = Optional.empty();
        private Predicate<String> tableFilter = t -> true;
        private UnaryOperator<String> tableRenamer = t -> t;
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

        public MaterializerBuilder tableRenamer(UnaryOperator<String> tableRenamer) {
            this.tableRenamer = tableRenamer;
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
