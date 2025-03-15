package hu.webarticum.holodb.jpa.annotation;

public @interface HoloValue {
    
    public enum Type {
        NULL, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, STRING, CLASS, LARGE_INTEGER, JSON
    }
    

    public boolean isGiven() default true;
    
    public Type type();
    
    public String key() default "";
    
    public boolean booleanValue() default false;
    
    public byte byteValue() default 0;
    
    public char charValue() default 0;
    
    public short shortValue() default 0;
    
    public int intValue() default 0;
    
    public long longValue() default 0;
    
    public float floatValue() default 0;
    
    public double doubleValue() default 0;
    
    public String stringValue() default "";
    
    public Class<?> classValue() default Void.class;
    
    public String largeIntegerValue() default "";
    
    public String json() default "";
    
}
