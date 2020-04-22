package hu.webarticum.holodb.query;

import java.util.Objects;

public class WherePart {

    private final Expression condition;
    
    
    public WherePart(Expression condition) {
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
        if (!(obj instanceof WherePart)) {
            return false;
        }
        
        return condition.equals(((WherePart) obj).condition);
    }
    
}
