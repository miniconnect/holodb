package hu.webarticum.holodb.jdbc.embedded;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

public class HoloEmbeddedJdbcDriver implements Driver {

    private static final HoloEmbeddedJdbcDriver INSTANCE = new HoloEmbeddedJdbcDriver();
    static {
        try {
            DriverManager.registerDriver(INSTANCE);
        } catch (SQLException e) {
            
            // TODO: logging?
            e.printStackTrace();
            
        }
    }
    
    
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        
        // FIXME
        return new HoloEmbeddedJdbcConnection();
        
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.startsWith(Constants.URL_PREFIX);
    }

    @Override
    public int getMajorVersion() {
        return Constants.MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return Constants.MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("java.util.logging is not used");
    }

}
