package hu.webarticum.holodb.core.query.runner;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface ValueHolder {

    public Class<?> getType();

    public boolean isNull();

    public Object get();
    
    public <T> T get(Class<T> clazz);

    public boolean getBoolean();
    
    public byte getByte();
    
    public char getChar();
    
    public short getShort();
    
    public int getInt();
    
    public long getLong();
    
    public float getFloat();
    
    public double getDouble();
    
    public BigInteger getBigInteger();
    
    public BigDecimal getBigDecimal();
    
    public String getString();
    
}
