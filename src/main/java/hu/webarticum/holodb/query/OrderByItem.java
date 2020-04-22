package hu.webarticum.holodb.query;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OrderByItem {

    public enum Direction {
        ASC,
        DESC,
    }
    
    
    private final Expression expression;
    
    private final Direction direction;
    

    public OrderByItem(Expression expression) {
        this(expression, Direction.ASC);
    }

    public OrderByItem(Expression expression, Direction direction) {
        this.expression = Objects.requireNonNull(expression);
        this.direction = Objects.requireNonNull(direction);
    }
    
    
    public Expression getExpression() {
        return expression;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(expression)
                .append(direction)
                .build();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderByItem)) {
            return false;
        }
        
        OrderByItem other = (OrderByItem) obj;
        return
                Objects.equals(expression, other.expression) &&
                Objects.equals(direction, other.direction);
    }
    
}
