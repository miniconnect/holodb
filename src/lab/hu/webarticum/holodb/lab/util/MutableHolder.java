package hu.webarticum.holodb.lab.util;

public class MutableHolder<T> {
    
    private T value;
    

    public MutableHolder() {
        this(null);
    }

    public MutableHolder(T value) {
        this.value = value;
    }
    
    
    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }
    
}
