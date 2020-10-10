package hu.webarticum.holodb.core.context;

import hu.webarticum.holodb.core.query.runner.ResultSet;

public class DefaultQueryResult implements QueryResult {

    private final boolean success;
    
    private final ResultSet resultSet;
    
    
    public DefaultQueryResult(boolean success) {
        this(success, null);
    }

    public DefaultQueryResult(ResultSet resultSet) {
        this(true, resultSet);
    }

    private DefaultQueryResult(boolean success, ResultSet resultSet) {
        this.success = success;
        this.resultSet = resultSet;
    }
    
    
    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public boolean hasResultSet() {
        return (resultSet != null);
    }

    @Override
    public ResultSet resultSet() {
        return resultSet;
    }

}
