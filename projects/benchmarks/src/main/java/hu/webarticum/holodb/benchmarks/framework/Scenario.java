package hu.webarticum.holodb.benchmarks.framework;

public interface Scenario {

    public String name();
    
    public String description();
    
    public void setUp() throws Exception;
    
    public void runOnce() throws Exception;
    
    public void tearDown() throws Exception;
    
}
