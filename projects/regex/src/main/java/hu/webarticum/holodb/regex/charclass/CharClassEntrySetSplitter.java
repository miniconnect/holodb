package hu.webarticum.holodb.regex.charclass;

import hu.webarticum.holodb.regex.charclass.CharClassSplitter.Containment;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class CharClassEntrySetSplitter<T> {

    public SimpleEntryList<CharClass, ImmutableList<T>> split(SimpleEntryList<CharClass, T> charClassEntrySet) {
        SimpleEntryList<CharClass, ImmutableList<T>> entries = new SimpleEntryList<>();
        for (SimpleEntryList.Entry<CharClass, T> sourceEntry : charClassEntrySet) {
            entries = splitNext(entries, sourceEntry);
        }
        return entries;
    }

    private SimpleEntryList<CharClass, ImmutableList<T>> splitNext(
            SimpleEntryList<CharClass, ImmutableList<T>> listEntries,
            SimpleEntryList.Entry<CharClass, T> nextEntry) {
        SimpleEntryList<CharClass, ImmutableList<T>> result = new SimpleEntryList<>();
        for (SimpleEntryList.Entry<CharClass, ImmutableList<T>> listEntry : listEntries) {
            CharClass previousCharClass = listEntry.key();
            ImmutableList<T> previousValues = listEntry.value();
            if (nextEntry == null) {
                result.add(previousCharClass, previousValues);
                continue;
            }
            CharClass nextCharClass = nextEntry.key();
            T nextValue = nextEntry.value();
            SimpleEntryList<CharClass, Containment> headEntries =
                    CharClassSplitter.of(previousCharClass, nextCharClass).split();
            SimpleEntryList.Entry<CharClass, Containment> tailEntry = headEntries.last();
            if (tailEntry.value() == Containment.RIGHT) {
                headEntries.removeLast();
                nextEntry = SimpleEntryList.Entry.of(tailEntry.key(), nextValue);
            } else {
                nextEntry = null;
            }
            for (SimpleEntryList.Entry<CharClass, Containment> headEntry : headEntries) {
                ImmutableList<T> mergedValues;
                switch (headEntry.value()) {
                    case LEFT:
                        mergedValues = previousValues;
                        break;
                    case RIGHT:
                        mergedValues = ImmutableList.of(nextValue);
                        break;
                    case BOTH:
                    default:
                        mergedValues = previousValues.append(nextValue);
                        break;
                }
                result.add(headEntry.key(), mergedValues);
            }
        }
        if (nextEntry != null) {
            result.add(nextEntry.key(), ImmutableList.of(nextEntry.value()));
        }
        return result;
    }

}
