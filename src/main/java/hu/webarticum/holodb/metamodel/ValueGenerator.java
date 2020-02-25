package hu.webarticum.holodb.metamodel;

import java.math.BigInteger;

public interface ValueGenerator<T> {

    public T generate(Column column, BigInteger rowIndex);
    
}
