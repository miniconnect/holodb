package hu.webarticum.holodb.core.query.model;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class Query {

    private final FieldsPart fieldsPart;
    
    private final FromPart fromPart;
    
    private final WherePart wherePart;
    
    private final GroupByPart groupByPart;
    
    private final HavingPart havingPart;
    
    private final OrderByPart orderByPart;
    
    private final LimitPart limitPart;
    
    private boolean hashCodeCalculated = false;
    
    private int hashCode;
    

    // FIXME
    public Query() {
        this((FieldsPart) null);
    }
    
    public Query(FieldsPart fieldsPart) {
        this(fieldsPart, null, null, null, null, null, null);
    }
    
    public Query(FromPart fromPart) {
        this(fromPart, null, null);
    }
    
    public Query(FromPart fromPart, WherePart wherePart, OrderByPart orderByPart) {
        this(null, fromPart, wherePart, null, null, orderByPart, null);
    }

    public Query(FieldsPart fieldsPart, FromPart fromPart, WherePart wherePart, GroupByPart groupByPart,
            HavingPart havingPart, OrderByPart orderByPart, LimitPart limitPart) {
        
        if (fieldsPart == null && fromPart == null) {
            throw new NullPointerException("Fields and from part can not both be null");
        }
        
        this.fieldsPart = fieldsPart;
        this.fromPart = fromPart;
        this.wherePart = wherePart;
        this.groupByPart = groupByPart;
        this.havingPart = havingPart;
        this.orderByPart = orderByPart;
        this.limitPart = limitPart;
    }

    
    public boolean hasFieldsPart() {
        return (fieldsPart != null);
    }
    
    public FieldsPart getFieldsPart() {
        return fieldsPart;
    }

    public boolean hasFromPart() {
        return (fromPart != null);
    }
    
    public FromPart getFromPart() {
        return fromPart;
    }

    public boolean hasWherePart() {
        return (wherePart != null);
    }
    
    public WherePart getWherePart() {
        return wherePart;
    }

    public boolean hasGroupByPart() {
        return (groupByPart != null);
    }
    
    public GroupByPart getGroupByPart() {
        return groupByPart;
    }

    public boolean hasHavingPart() {
        return (havingPart != null);
    }
    
    public HavingPart getHavingPart() {
        return havingPart;
    }

    public boolean hasOrderByPart() {
        return (orderByPart != null);
    }
    
    public OrderByPart getOrderByPart() {
        return orderByPart;
    }

    public boolean hasLimitPart() {
        return (limitPart != null);
    }
    
    public LimitPart getLimitPart() {
        return limitPart;
    }
    
    @Override
    public int hashCode() {
        if (!hashCodeCalculated) {
            hashCode = calculateHashCode();
            hashCodeCalculated = true;
        }
        return hashCode;
    }

    private int calculateHashCode() {
        return new HashCodeBuilder()
                .append(fieldsPart)
                .append(fromPart)
                .append(wherePart)
                .append(groupByPart)
                .append(havingPart)
                .append(orderByPart)
                .append(limitPart)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Query)) {
            return false;
        }
        
        Query other = (Query) obj;
        
        if (hashCode != other.hashCode) {
            return false;
        }
        
        return
                Objects.equals(fieldsPart, other.fieldsPart) &&
                Objects.equals(fromPart, other.fromPart) &&
                Objects.equals(wherePart, other.wherePart) &&
                Objects.equals(groupByPart, other.groupByPart) &&
                Objects.equals(havingPart, other.havingPart) &&
                Objects.equals(orderByPart, other.orderByPart) &&
                Objects.equals(limitPart, other.limitPart);
    }
    
    @Override
    public String toString() {
        
        // FIXME
        // new SqlQueryStringifier(...).xxxx(...)
        return "SELECT \"lorem\" FROM \"ipsum\"";
        
    }

}
