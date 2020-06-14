package hu.webarticum.holodb.core.query.model;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public final class FieldsItem implements Aliasable {
    
    private final Expression expression;
    
    private final String alias;
    

    public FieldsItem(Expression expression) {
        this(expression, null);
    }

    public FieldsItem(Expression expression, String alias) {
        this.expression = Objects.requireNonNull(expression, "Expression can not be null");
        this.alias = alias;
    }
    
    
    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean hasAlias() {
        return (alias != null);
    }

    @Override
    public String getAlias() {
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
