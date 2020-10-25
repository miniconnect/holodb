package hu.webarticum.holodb.query.common;

public interface Result {
    
    // TODO: status, error, warnings...

    public boolean hasResultSet();
    
    public ResultSet resultSet();
    
}
