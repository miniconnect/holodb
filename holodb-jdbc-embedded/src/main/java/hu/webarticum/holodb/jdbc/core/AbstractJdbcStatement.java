package hu.webarticum.holodb.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractJdbcStatement implements Statement {

    private final AbstractJdbcConnection connection;
    
    
    protected AbstractJdbcStatement(AbstractJdbcConnection connection) {
        this.connection = connection;
    }
    
    
    @Override
    public Connection getConnection() throws SQLException {
        return getConnectionInternal();
    }

    protected AbstractJdbcConnection getConnectionInternal() throws SQLException {
        return connection;
    }

}
