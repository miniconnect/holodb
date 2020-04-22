package hu.webarticum.holodb.query.model;

import java.util.Objects;

public final class HavingPart {

    private final Expression condition;
    
    
    public HavingPart(Expression condition) {
        this.condition = Objects.requireNonNull(condition);
    }
    
    
    public Expression getCondition() {
        return condition;
    }
    
    @Override
    public int hashCode() {
        return condition.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HavingPart)) {
            return false;
        }
        
        return condition.equals(((HavingPart) obj).condition);
    }
    
}
