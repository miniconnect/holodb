package hu.webarticum.holodb.admin.dump;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.webarticum.holodb.admin.util.NameTransformer;
import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.StorageAccessFactory;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.storage.api.NamedResourceStore;
import hu.webarticum.minibase.storage.api.Schema;
import hu.webarticum.minibase.storage.api.StorageAccess;
import hu.webarticum.miniconnect.lang.ImmutableList;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "materialize",
        description = "Materializes a HoloDB virtual dataset",
        footer = "%n" + NameTransformer.HELP_TEXT,
        mixinStandardHelpOptions = true)
public class MaterializeCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Option(
            names = { "-j", "--jdbc-url" },
            description = "JDBC URL to connect to",
            required = true)
    private String jdbcUrl;

    @Option(
            names = { "-u", "--username" },
            description = "Database username")
    private String username;

    @Option(
            names = { "-p", "--password" }, 
            description = "Database password",
            prompt = "Password: ",
            interactive = true, 
            arity = "0..1")
    private String password;

    @Option(
            names = { "-D", "--dialect" },
            description = "SQL dialect to render")
    private String sqlDialectName;

    @Option(
            names = { "-c", "--config-file" },
            description = "Source HoloDB config file",
            required = true)
    private String configFilePath;

    @Option(
            names = { "-s", "--source-schema" },
            description = "Source schema name")
    private String sourceSchemaName;

    @Option(
            names = { "-f", "--table-filter" },
            description = "Source table filter regex")
    private String tableFilterRegex;

    @Option(
            names = { "-F", "--column-filter" },
            description = "Source column filter regex (fully qualified)")
    private String columnFilterRegex;

    @Option(
            names = { "-r", "--rename" },
            description = "General rename template")
    private String renameTemplate;

    @Option(
            names = { "-T", "--table-rename" },
            description = "Table rename template")
    private String tableRenameTemplate;

    @Option(
            names = { "-C", "--column-rename" },
            description = "Column rename template")
    private String columnRenameTemplate;

    @Option(
            names = { "-I", "--index-rename" },
            description = "Index rename template (default: 'idx_' + general rename)")
    private String indexRenameTemplate;

    @Option(
            names = { "-x", "--index-rename-no-table" },
            description = "Omits table name from index names",
            defaultValue = "false")
    private boolean indexRenameNoTable;

    @Option(
            names = { "-d", "--drop" },
            description = "Drops existing target tables",
            defaultValue = "false")
    private boolean drop;

    @Option(
            names = { "-N", "--no-create" },
            description = "Skips table creation",
            defaultValue = "false")
    private boolean noCreate;

    @Option(
            names = { "-n", "--no-insert" },
            description = "Skips insertion of data rows",
            defaultValue = "false")
    private boolean noInsert;

    @Option(
            names = { "--dry-run" },
            description = "Dry-run mode, no query will be executed",
            defaultValue = "false")
    private boolean dryRun;

    @Override
    public Integer call() throws Exception {
        try (Connection connection = connect()) {
            materialize(connection);
            return 0;
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void materialize(Connection connection) {
        HoloConfig config = new ConfigLoader(new File(configFilePath)).load();
        StorageAccess storageAccess = StorageAccessFactory.createStorageAccess(config);
        NamedResourceStore<Schema> schemas = storageAccess.schemas();
        if (schemas.names().isEmpty()) {
            throw new IllegalArgumentException("No source schema found");
        }
        if (sourceSchemaName != null && !schemas.contains(sourceSchemaName)) {
            throw new IllegalArgumentException("Source schema not found: " + sourceSchemaName);
        }
        DSLContext dslContext = createDSLContext(connection);
        Dumper.DumperBuilder builder = Dumper.builder(storageAccess, dslContext)
                .sourceSchemaName(sourceSchemaName)
                .dropTables(drop)
                .createTables(!noCreate)
                .insertData(!noInsert);
        if (tableFilterRegex != null) {
            builder.tableFilter(Pattern.compile(tableFilterRegex).asMatchPredicate());
        }
        if (columnFilterRegex != null) {
            builder.columnFilter(Pattern.compile(columnFilterRegex).asMatchPredicate());
        }
        createNameTransformer(tableRenameTemplate).ifPresent(t -> builder.tableRenamer(t::transform));
        Optional<NameTransformer> columnNameTransformer = createNameTransformer(columnRenameTemplate);
        columnNameTransformer.ifPresent(t -> builder.columnRenamer(t::transform));
        builder.indexNamer(createIndexNamer());

        Dumper dumper = builder.build(this::executeQuery);
        if (dryRun) {
            System.out.println("DRY-RUN!");
            logger.info("Started in dry-run mode, no query will be executed");
        }
        dumper.dump();
    }

    private DSLContext createDSLContext(Connection connection) {
        if (sqlDialectName == null) {
            return DSL.using(connection);
        }
        SQLDialect sqlDialect = SQLDialect.valueOf(sqlDialectName.toUpperCase());
        return DSL.using(connection, sqlDialect);
    }

    private Optional<NameTransformer> createNameTransformer(String specificTemplate) {
        String effectiveTemplate = specificTemplate != null ? specificTemplate : renameTemplate;
        return Optional.ofNullable(effectiveTemplate).map(NameTransformer::parse);
    }

    private BiFunction<String, ImmutableList<String>, String> createIndexNamer() {
        BiFunction<String, ImmutableList<String>, String> result = indexRenameNoTable ?
                (t, cs) -> String.join("_", cs) :
                (t, cs) -> t + "_" + String.join("_", cs);

        if (indexRenameTemplate != null) {
            return result.andThen(NameTransformer.parse(indexRenameTemplate)::transform);
        }

        result = result.andThen(v -> "idx_" + v);
        if (renameTemplate != null) {
            result = result.andThen(NameTransformer.parse(renameTemplate)::transform);
        }
        return result;
    }

    private void executeQuery(Query query) {
        if (logger.isDebugEnabled()) {
            logger.debug("SQL: " + query.getSQL());
        }
        if (!dryRun) {
            query.execute();
        }
    }

}
