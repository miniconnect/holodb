package hu.webarticum.holodb.jdbc.core;

import java.sql.PreparedStatement;

public abstract class AbstractJdbcPreparedStatement extends AbstractJdbcStatement implements PreparedStatement {

    protected AbstractJdbcPreparedStatement(AbstractJdbcConnection connection) {
        super(connection);
    }
    
}
