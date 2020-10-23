package hu.webarticum.holodb.jdbc.embedded;

import hu.webarticum.holodb.jdbc.core.AbstractJdbcConnection;
import hu.webarticum.holodb.jdbc.core.AbstractJdbcResultSet;
import hu.webarticum.holodb.jdbc.core.AbstractJdbcStatement;

public class HoloEmbeddedJdbcResultSet extends AbstractJdbcResultSet {

    // TODO
    
    
    
    public HoloEmbeddedJdbcResultSet(
            AbstractJdbcConnection connection, AbstractJdbcStatement statement) {
        
        super(connection, statement);
        
        // TODO
        
    }

    
    @Override
    protected void closeInternal() throws Exception {
        // TODO
    }
    
}
