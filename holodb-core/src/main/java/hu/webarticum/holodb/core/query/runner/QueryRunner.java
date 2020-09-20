package hu.webarticum.holodb.core.query.runner;

import hu.webarticum.holodb.core.query.model.Query;

public interface QueryRunner {

    public ResultSet execute(Query query);
    
}
