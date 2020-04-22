package hu.webarticum.holodb.query;

import java.util.Objects;

public class Alias {

    private final String name;
    
    
    public Alias(String name) {
        this.name = Objects.requireNonNull(name);
    }
    
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Alias)) {
            return false;
        }
        
        return name.equals(((Alias) obj).name);
    }
    
}
