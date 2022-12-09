package hu.webarticum.holodb.app.misc;

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

import com.ibm.icu.text.Collator;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;
import hu.webarticum.strex.Strex;

// TODO: extract its general part to the core project
public class StrexSource implements SortedSource<String> {

    private final Strex strex;
    

    public StrexSource(Strex strex) {
        this.strex = strex;
    }
    
    
    @Override
    public Class<String> type() {
        return String.class;
    }
    
    @Override
    public LargeInteger size() {
        return LargeInteger.of(strex.size());
    }

    @Override
    public String get(LargeInteger index) {
        return strex.get(index.bigIntegerValue());
    }
    
    @Override
    public Comparator<String> comparator() {
        return Collator.getInstance(Locale.US)::compare;
    }

    @Override
    public Optional<ImmutableList<String>> possibleValues() {
        return Optional.empty();
    }
    
    @Override
    public Range find(Object value) {
        String stringValue = (String) value;
        LargeInteger position = LargeInteger.of(strex.indexOf(stringValue));
        return position.isNonNegative() ?
                Range.fromSize(position, LargeInteger.ONE) :
                Range.fromSize(position.negate().subtract(LargeInteger.ONE), LargeInteger.ZERO);
    }

    @Override
    public Range findBetween(
            Object minValue, boolean minInclusive, Object maxValue, boolean maxInclusive) {
        if (minValue != null && maxValue != null) {
            int cmp = comparator().compare((String) minValue, (String) maxValue);
            if (cmp > 0 || (cmp == 0 && !minInclusive && !maxInclusive)) {
                return Range.empty(find(minValue).from());
            }
        }
        
        LargeInteger from;
        if (minValue != null) {
            Range minRange = find(minValue);
            from = minInclusive ? minRange.from() : minRange.until();
        } else {
            from = LargeInteger.ZERO;
        }
        
        LargeInteger until;
        if (maxValue != null) {
            Range maxRange = find(maxValue);
            until = maxInclusive ? maxRange.until() : maxRange.from();
        } else {
            until = size();
        }
        
        return Range.fromUntil(from, until);
    }

    @Override
    public Range findNulls() {
        return Range.empty();
    }
    
}
