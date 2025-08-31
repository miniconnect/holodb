package hu.webarticum.holodb.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention( RetentionPolicy.RUNTIME )
@Repeatable(HoloVirtualColumns.class)
public @interface HoloVirtualColumn {

    public String name();

    public Class<?> type();

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

    public HoloColumnDummyTextKind valuesTextKind() default HoloColumnDummyTextKind.UNDEFINED;
    
    public String[] valuesForeignColumn() default {};
    
    public HoloColumnDistributionQuality distributionQuality() default HoloColumnDistributionQuality.UNDEFINED;

    public HoloColumnShuffleQuality shuffleQuality() default HoloColumnShuffleQuality.UNDEFINED;
    
    public Class<?> sourceFactory() default Void.class;

    public HoloValue sourceFactoryData() default @HoloValue(isGiven = false, type = HoloValue.Type.NULL);

    public HoloValue[] sourceFactoryDataMap() default {};

    public HoloValue defaultValue() default @HoloValue(isGiven = false, type = HoloValue.Type.NULL);
    
}
