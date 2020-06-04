package hu.webarticum.holodb.util;

import java.util.function.Consumer;

public final class ObjectUtil {
    
    private ObjectUtil() {
    }
    
    
    public static <T> T apply(T object, Consumer<T> operation) {
        operation.accept(object);
        return object;
    }

}
