package hu.webarticum.holodb.core.context;

public interface UpdateableContext {

    // TODO: transactions etc.
    
    public Result executeUpdate();
    
}
