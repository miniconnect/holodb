package hu.webarticum.holodb.admin.materializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.inject.Singleton;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Singleton
@Command(
        name = "materialize",
        description = "Materializes a HoloDB virtual dataset",
        mixinStandardHelpOptions = true
)
public class MaterializerCommand implements Runnable {

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
            System.out.println(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

}
