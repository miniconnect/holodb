package hu.webarticum.holodb.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention( RetentionPolicy.RUNTIME )
public @interface HoloColumn {

    public String name() default "";

    public Class<?> type() default Void.class;

    public HoloColumnMode mode() default HoloColumnMode.UNDEFINED;

    public long nullCount() default -1L;

    public String largeNullCount() default "";
    
    public String[] values() default {};
    
    public String valuesResource() default "";
    
    public String valuesBundle() default "";
    
    public long[] valuesRange() default {};
    
    public String[] largeValuesRange() default {};
    
    public String valuesPattern() default "";
    
    public String valuesDynamicPattern() default "";
    
    public String[] valuesForeignColumn() default {};
    
}
