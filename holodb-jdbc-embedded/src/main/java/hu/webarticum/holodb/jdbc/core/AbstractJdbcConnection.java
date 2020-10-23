package hu.webarticum.holodb.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

// TODO: move jdbc.core to separate project (jdbc.client doesn't require jdbc.embedded)

public abstract class AbstractJdbcConnection implements Connection {
    
    private volatile int holdability = ResultSet.HOLD_CURSORS_OVER_COMMIT;
    
    private volatile boolean closed = false;
    

    @Override
    public boolean isWrapperFor(Class<?> type) throws SQLException {
        return type != null && type.isAssignableFrom(getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) throws SQLException {
        if (!isWrapperFor(type)) {
            throw new IllegalArgumentException(String.format("Can not cast to %s", type));
        }

        return (T) this;
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return getClientInfo().getProperty(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        checkClosed();
        // FIXME
        return new Properties();
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        // FIXME
        // not supported
        throw new SQLClientInfoException();
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        // FIXME
        // not supported
        throw new SQLClientInfoException();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        checkClosed();
        // FIXME
        return 0;
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        checkClosed();
        // FIXME / TODO
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        checkClosed();
        // FIXME
        // currently HoloDB is read-only
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkClosed();
        // FIXME
        // nothing to do, currently HoloDB is read-only
    }

    @Override
    public int getHoldability() throws SQLException { // NOSONAR
        checkClosed();
        return holdability;
    }

    @Override
    public synchronized void setHoldability(int holdability) throws SQLException {
        checkClosed();
        this.holdability = holdability;
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        if (map != null && map.size() > 0) {
            throw new IllegalArgumentException("Non-empty type map is not supported");
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        return createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, holdability);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return createStatement(resultSetType, resultSetConcurrency, holdability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, holdability);
    }

    @Override
    public PreparedStatement prepareStatement(
            String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        
        return prepareStatement(sql, resultSetType, resultSetConcurrency, holdability);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        // FIXME: SQL-92 compatibility?
        return sql;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        // FIXME
        // currently not supported
        // NOTE: the same instance must be returned (until clearWarnings()) 
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // FIXME
        // currently not supported
        // NOTE: new instance must be created
    }

    @Override
    public synchronized void abort(Executor executor) {
        if (closed) {
            return;
        }
        
        closed = true;
        executor.execute(() -> {
            try {
                this.closeInternal();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception", e); // NOSONAR
            }
        });
    }

    @Override
    public synchronized void close() throws SQLException {
        if (closed) {
            return;
        }
        
        closed = true;
        
        try {
            closeInternal();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            
            // TODO
            
        }
    }

    protected void checkClosed() throws SQLException {
        if (isClosed()) {
            throw new SQLException("Connection is closed");
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return closed;
    }

    protected abstract void closeInternal() throws Exception; // NOSONAR

}
