package hu.webarticum.holodb.core.query.runner;

public interface ResultRecord {

    public ValueHolder get(int nth);

    public ValueHolder get(String name);
    
}
