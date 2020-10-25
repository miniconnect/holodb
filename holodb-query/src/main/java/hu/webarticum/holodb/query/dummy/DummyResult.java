package hu.webarticum.holodb.query.dummy;

import hu.webarticum.holodb.query.common.Result;
import hu.webarticum.holodb.query.common.ResultSet;

public class DummyResult implements Result {

    private final ResultSet resultSet;
    
    
    public DummyResult(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
    
    
    @Override
    public boolean hasResultSet() {
        return resultSet != null;
    }

    @Override
    public ResultSet resultSet() {
        return resultSet;
    }

}
