package hu.webarticum.holodb.core.metamodel;

public class HintKey<T> {

    private Class<T> valueType;
    
    
    public HintKey(Class<T> valueType) {
        this.valueType = valueType;
    }
    
    
    public Class<T> getValueType() {
        return valueType;
    }
    
}