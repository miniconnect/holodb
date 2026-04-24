package hu.webarticum.holodb.admin.materialize;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Pattern;

import hu.webarticum.holodb.admin.util.NameTransformer;
import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.StorageAccessFactory;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.storage.api.StorageAccess;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "materialize",
        description = "Materializes a HoloDB virtual dataset",
        mixinStandardHelpOptions = true)
public class MaterializeCommand implements Runnable {

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
            names = { "-r", "--table-rename" },
            description = "Table rename template (for example: 'prefix_{clean|lower|10}_suffix')")
    private String tableRenameTemplate;

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

    @Override
    public void run() {
        try (Connection connection = connect()) {
            materialize(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    private void materialize(Connection connection) {
        HoloConfig config = new ConfigLoader(new File(configFilePath)).load();
        StorageAccess storageAccess = StorageAccessFactory.createStorageAccess(config);
        Materializer.MaterializerBuilder builder = Materializer.builder(storageAccess, connection);
        if (sourceSchemaName != null) {
            builder.sourceSchemaName(sourceSchemaName);
        }
        if (tableFilterRegex != null) {
            builder.tableFilter(Pattern.compile(tableFilterRegex).asMatchPredicate());
        }
        if (tableRenameTemplate != null) {
            builder.tableRenamer(NameTransformer.parse(tableRenameTemplate)::transform);
        }
        Materializer materializer = builder.build();
        materializer.materialize();
    }

}
