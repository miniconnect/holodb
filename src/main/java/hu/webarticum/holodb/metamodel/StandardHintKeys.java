package hu.webarticum.holodb.metamodel;

import java.math.BigInteger;

// FIXME: name?
// FIXME: place?
// FIXME: hints vs. specific setting (e. g. string semantics)?
//          (possible solution: define hint key closer to the semantic)

public class StandardHintKeys {
    
    public static final HintKey<BigInteger> TABLE_MIN_ROW_COUNT = new HintKey<>(BigInteger.class);

    public static final HintKey<BigInteger> TABLE_MAX_ROW_COUNT = new HintKey<>(BigInteger.class);
    
    public static final HintKey<BigInteger> COLUMN_VALUE_INTEGRAL_MIN_VALUE = new HintKey<>(BigInteger.class);
    
    public static final HintKey<BigInteger> COLUMN_VALUE_INTEGRAL_MAX_VALUE = new HintKey<>(BigInteger.class);
    
    public static final HintKey<Integer> COLUMN_VALUE_TEXTUAL_MIN_LENGTH = new HintKey<>(Integer.class);
    
    public static final HintKey<Integer> COLUMN_VALUE_TEXTUAL_MAX_LENGTH = new HintKey<>(Integer.class);
    
    // FIXME: COLUMN_VALUE_GENERATOR[_FACTORY] ???
    //          table1:things[3]:parent.id (shortname of association... lookup per table)
    //           ...inverted associations?

    
    private StandardHintKeys() {
    }
    
}
