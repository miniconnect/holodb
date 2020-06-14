package hu.webarticum.holodb.core.query.model;

public interface Aliasable {

    public boolean hasAlias();
    
    public String getAlias();
    
    // FIXME: calculateAlias() here?
    
}
