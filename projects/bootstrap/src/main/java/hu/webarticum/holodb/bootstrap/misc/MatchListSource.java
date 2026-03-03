package hu.webarticum.holodb.bootstrap.misc;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

import hu.webarticum.holodb.core.data.selection.Range;
import hu.webarticum.holodb.core.data.source.SortedSource;
import hu.webarticum.holodb.regex.facade.MatchList;
import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class MatchListSource implements SortedSource<String> {

    private final MatchList matchList;


    public MatchListSource(MatchList matchList) {
        this.matchList = matchList;
    }


    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public LargeInteger size() {
        return matchList.size();
    }

    @Override
    public String get(LargeInteger index) {
        return matchList.get(index);
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
        String stringValue = value == null ? null : value.toString();
        FindPositionResult result = matchList.find(stringValue);
        return Range.fromSize(result.position(), result.found() ? LargeInteger.ONE : LargeInteger.ZERO);
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
