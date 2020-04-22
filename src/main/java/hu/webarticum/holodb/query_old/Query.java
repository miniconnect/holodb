package hu.webarticum.holodb.query_old;

import java.util.List;
import java.util.Map;

public class Query {
    
    public Query(
            List<QueryExpression> fields, // FIXME
            Map<String, QueryIdentifier> tables, // FIXME
            Object joins, // TODO
            QueryExpression where,
            Object groupBy, // TODO
            Object orderBy, // TODO
            QueryLimit limit,
            QueryExpression having) {
        
        // TODO
    }
    
}
