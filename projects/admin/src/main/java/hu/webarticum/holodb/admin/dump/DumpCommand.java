package hu.webarticum.holodb.admin.dump;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.jooq.DDLQuery;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderFormatting;
import org.jooq.conf.RenderKeywordCase;
import org.jooq.conf.Settings;
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
        name = "dump",
        description = "Dump a HoloDB virtual dataset to SQL text",
        footer = "%n" + NameTransformer.HELP_TEXT,
        mixinStandardHelpOptions = true)
public class DumpCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final Settings DDL_RENDERING_SETTINGS = new Settings()
            .withRenderFormatted(true)
            .withRenderKeywordCase(RenderKeywordCase.UPPER)
            .withRenderFormatting(new RenderFormatting().withPrintMargin(200));

    private static final Settings DML_RENDERING_SETTINGS = new Settings().withRenderFormatted(false);

    @Option(
            names = { "-o", "--output-file" },
            description = "Output file path (default: stdout)")
    private String outputFilePath;

    @Option(
            names = { "-k", "--no-overwrite" },
            description = "Stops if the output file already exists",
            defaultValue = "false")
    private boolean noOverride;

    @Option(
            names = { "-D", "--dialect" },
            description = "SQL dialect to render",
            required = true)
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

    @Override
    public Integer call() throws Exception {
        try {
            dump();
            return 0;
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    private void dump() {
        HoloConfig config = new ConfigLoader(new File(configFilePath)).load();
        StorageAccess storageAccess = StorageAccessFactory.createStorageAccess(config);
        NamedResourceStore<Schema> schemas = storageAccess.schemas();
        if (schemas.names().isEmpty()) {
            throw new IllegalArgumentException("No source schema found");
        }
        if (sourceSchemaName != null && !schemas.contains(sourceSchemaName)) {
            throw new IllegalArgumentException("Source schema not found: " + sourceSchemaName);
        }
        DSLContext dslContext = createDSLContext();
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

        Appendable out = openOut();
        Dumper dumper = builder.build(q -> dumpQuery(q, out));
        try {
            dumper.dump();
        } finally {
            closeOut(out);
        }
    }

    private DSLContext createDSLContext() {
        SQLDialect sqlDialect = SQLDialect.valueOf(sqlDialectName.toUpperCase());
        return DSL.using(sqlDialect);
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

    private Appendable openOut() {
        if (outputFilePath == null) {
            return System.out;
        }

        Path path = Path.of(outputFilePath);
        OutputStream outStream;
        try {
            if (noOverride) {
                outStream = Files.newOutputStream(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            } else {
                outStream = Files.newOutputStream(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to open dump file: " + outputFilePath, e);
        }
        return new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
    }

    private void closeOut(Appendable appendable) {
        if (appendable != System.out && appendable instanceof AutoCloseable) {
            try {
                ((AutoCloseable) appendable).close();
            } catch (Exception e) {
                throw new UncheckedIOException("Failed to close the output stream", new IOException(e));
            }
        }
    }

    private void dumpQuery(Query query, Appendable out) {
        Settings settings = query instanceof DDLQuery ? DDL_RENDERING_SETTINGS : DML_RENDERING_SETTINGS;
        DSLContext temporaryContext = DSL.using(query.configuration().dialect(), settings);
        String renderedQuery = temporaryContext.renderInlined(temporaryContext.queries(query));
        try {
            out.append(renderedQuery);
            out.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write to the output stream", e);
        }
    }

}
