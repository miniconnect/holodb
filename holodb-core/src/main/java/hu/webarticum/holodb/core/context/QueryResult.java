package hu.webarticum.holodb.core.context;

import hu.webarticum.holodb.core.query.runner.ResultSet;

public interface QueryResult extends Result {

    public boolean hasResultSet();
    
    public ResultSet resultSet();
    
}
