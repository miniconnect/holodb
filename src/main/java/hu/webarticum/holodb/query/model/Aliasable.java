package hu.webarticum.holodb.query.model;

public interface Aliasable {

    public boolean hasAlias();
    
    public String getAlias();
    
    // FIXME: calculateAlias() here?
    
}
