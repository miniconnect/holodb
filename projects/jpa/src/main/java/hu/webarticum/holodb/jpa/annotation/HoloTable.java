package hu.webarticum.holodb.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention( RetentionPolicy.RUNTIME )
public @interface HoloTable {

    public String schema() default "";

    public String name() default "";

    public HoloWriteable writeable() default HoloWriteable.UNDEFINED;

    public long size() default -1L;

    public String largeSize() default "";
    
}
