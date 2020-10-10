package hu.webarticum.holodb.core.query.parser;

import hu.webarticum.holodb.core.query.model.Query;

public interface SqlQueryParser {

    public Query parse(String sql);
    
}
