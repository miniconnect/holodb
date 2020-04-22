package hu.webarticum.holodb.query;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FieldsItem {
    
    private final Expression expression;
    
    private final Alias alias;
    

    public FieldsItem(Expression expression) {
        this(expression, null);
    }

    public FieldsItem(Expression expression, Alias alias) {
        this.expression = Objects.requireNonNull(expression);
        this.alias = Objects.requireNonNull(alias);
    }
    
    
    public Expression getExpression() {
        return expression;
    }
    
    public boolean hasAlias() {
        return (alias != null);
    }
    
    public Alias getAlias() {
        return alias;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(expression)
                .append(alias)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FieldsItem)) {
            return false;
        }
        
        FieldsItem other = (FieldsItem) obj;
        return
                Objects.equals(expression, other.expression) &&
                Objects.equals(alias, other.alias);
    }
    
}
