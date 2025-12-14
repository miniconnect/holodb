package hu.webarticum.holodb.embedded;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.holodb.bootstrap.factory.ConfigLoader;
import hu.webarticum.holodb.bootstrap.factory.EngineBuilder;
import hu.webarticum.holodb.config.HoloConfig;
import hu.webarticum.minibase.engine.api.Engine;
import hu.webarticum.minibase.engine.facade.FrameworkSession;
import hu.webarticum.minibase.engine.facade.FrameworkSessionManager;
import hu.webarticum.miniconnect.jdbc.MiniJdbcConnection;
import hu.webarticum.miniconnect.jdbc.MiniJdbcDriver;
import hu.webarticum.miniconnect.jdbc.provider.DatabaseProvider;
import hu.webarticum.miniconnect.jdbc.provider.impl.BlanketDatabaseProvider;

public class HoloEmbeddedDriver implements Driver {

    public static final String URL_PREFIX = "jdbc:holodb:embedded:";

    public static final Pattern TAIL_PATTERN = Pattern.compile(
            "^(?:file://(?<file>[^\\?]+)|resource://(?<resource>[^\\?]+))(?:\\?(?<properties>.*))?");

    public static final String FILE_GROUPNAME = "file";

    public static final String RESOURCE_GROUPNAME = "resource";

    public static final String PROPERTIES_GROUPNAME = "properties";

    public static final String SCHEMA_KEYNAME = "schema";


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

        String resourcePath = matcher.group(RESOURCE_GROUPNAME);
        if (resourcePath != null) {
            configLoader = new ConfigLoader(resourcePath);
        } else {
            String filePath = matcher.group(FILE_GROUPNAME);
            configLoader = new ConfigLoader(new File(filePath));
        }

        Map<String, String> properties = parseProperties(matcher.group(PROPERTIES_GROUPNAME));

        HoloConfig config = configLoader.load();
        Engine engine = EngineBuilder.ofConfig(config).build();
        FrameworkSessionManager sessionManager = new FrameworkSessionManager(engine);
        FrameworkSession session = sessionManager.openSession();
        if (properties.containsKey(SCHEMA_KEYNAME)) {
            session.engineSession().state().setCurrentSchema(properties.get(SCHEMA_KEYNAME));
        }
        DatabaseProvider databaseProvider = new BlanketDatabaseProvider();
        return new MiniJdbcConnection(session, databaseProvider);
    }

    private Map<String, String> parseProperties(String propertiesString) {
        Map<String, String> properties = new HashMap<>();
        if (propertiesString == null || propertiesString.isEmpty()) {
            return properties;
        }

        for (String entryString : propertiesString.split("&")) {
            String[] keyValue = entryString.split("=");
            String key = decodeUrlValue(keyValue[0]);
            String value = decodeUrlValue(keyValue[1]);
            properties.put(key, value);
        }

        return properties;
    }

    private String decodeUrlValue(String rawValue) {
        try {
            return URLDecoder.decode(rawValue, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return rawValue;
        }
    }

}
