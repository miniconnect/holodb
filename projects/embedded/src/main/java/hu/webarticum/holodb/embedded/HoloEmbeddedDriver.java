package hu.webarticum.holodb.embedded;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.holodb.app.config.HoloConfig;
import hu.webarticum.holodb.app.factory.ConfigLoader;
import hu.webarticum.holodb.app.factory.EngineBuilder;
import hu.webarticum.minibase.query.execution.QueryExecutor;
import hu.webarticum.minibase.query.execution.impl.IntegratedQueryExecutor;
import hu.webarticum.minibase.query.parser.AntlrSqlParser;
import hu.webarticum.minibase.query.parser.SqlParser;
import hu.webarticum.minibase.session.engine.Engine;
import hu.webarticum.minibase.session.facade.FrameworkSessionManager;
import hu.webarticum.miniconnect.api.MiniSession;
import hu.webarticum.miniconnect.api.MiniSessionManager;
import hu.webarticum.miniconnect.jdbc.MiniJdbcConnection;
import hu.webarticum.miniconnect.jdbc.MiniJdbcDriver;
import hu.webarticum.miniconnect.jdbc.provider.DatabaseProvider;
import hu.webarticum.miniconnect.jdbc.provider.impl.BlanketDatabaseProvider;

public class HoloEmbeddedDriver implements Driver {
    
    public static final String URL_PREFIX = "jdbc:holodb:embedded:";
    
    public static final Pattern TAIL_PATTERN = Pattern.compile(
            "^(?:file://(?<file>[^\\?]+)|resource://(?<resource>[^\\?]+))(?:\\?.*)?");
    

    @Override
    public boolean acceptsURL(String url) {
        return url.startsWith(URL_PREFIX);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return MiniJdbcDriver.DRIVER_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return MiniJdbcDriver.DRIVER_MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        String tail = url.substring(URL_PREFIX.length());
        Matcher matcher = TAIL_PATTERN.matcher(tail);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid connection url: " + url);
        }
        
        ConfigLoader configLoader;
        
        String resourcePath = matcher.group("resource");
        if (resourcePath != null) {
            configLoader = new ConfigLoader(resourcePath);
        } else {
            String filePath = matcher.group("file");
            configLoader = new ConfigLoader(new File(filePath));
        }
        
        SqlParser sqlParser = new AntlrSqlParser();
        QueryExecutor queryExecutor = new IntegratedQueryExecutor();
        DatabaseProvider databaseProvider = new BlanketDatabaseProvider();
        HoloConfig config = configLoader.load();
        Engine engine = EngineBuilder.ofConfig(config)
                .sqlParser(sqlParser)
                .queryExecutor(queryExecutor)
                .build();
        MiniSessionManager sessionManager = new FrameworkSessionManager(engine);
        MiniSession session = sessionManager.openSession();
        return new MiniJdbcConnection(session, databaseProvider);
    }
    
}
