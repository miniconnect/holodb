package hu.webarticum.holodb.query;

public class Query {

    private final FieldsPart fieldsPart;
    
    // TODO: from + joins
    
    private final WherePart wherePart;
    
    private final GroupByPart groupByPart;
    
    private final HavingPart havingPart;
    
    private final OrderByPart orderByPart;
    
    private final LimitPart limitPart;
    
    
    public Query( /* TODO */ ) {
        // TODO
        this(null, null, null, null, null, null);
    }

    public Query(FieldsPart fieldsPart, /* TODO: from + joins */ WherePart wherePart, GroupByPart groupByPart,
            HavingPart havingPart, OrderByPart orderByPart, LimitPart limitPart) {
        this.fieldsPart = fieldsPart;
        // TODO: from + joins
        this.wherePart = wherePart;
        this.groupByPart = groupByPart;
        this.havingPart = havingPart;
        this.orderByPart = orderByPart;
        this.limitPart = limitPart;
    }
    
    
    // TODO: methods...
    
    
    // parser
    // stringifier
    // analyzer (e. g. XXX.hasAggregateFields(query))
    // (other question: dumpers to other dialects - unimportant)
    
}
