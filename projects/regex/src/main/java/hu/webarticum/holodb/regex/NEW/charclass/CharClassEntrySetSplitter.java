package hu.webarticum.holodb.regex.NEW.charclass;

import hu.webarticum.holodb.regex.NEW.charclass.CharClassSplitter.Containment;
import hu.webarticum.miniconnect.lang.ImmutableList;

public class CharClassEntrySetSplitter<T> {

    private final SortedEntrySet<CharClass, T> charClassEntrySet;
    
    private CharClassEntrySetSplitter(SortedEntrySet<CharClass, T> charClassMap) {
        this.charClassEntrySet = charClassMap;
    }

    public static <T> CharClassEntrySetSplitter<T> of(SortedEntrySet<CharClass, T> charClassMap) {
        return new CharClassEntrySetSplitter<>(charClassMap);
    }
    
    public SortedEntrySet<CharClass, ImmutableList<T>> split() {
        SortedEntrySet<CharClass, ImmutableList<T>> entries = new SortedEntrySet<>();
        for (SortedEntrySet.Entry<CharClass, T> sourceEntry : charClassEntrySet) {
            entries = splitNext(entries, sourceEntry);
        }
        return entries;
    }
    
    private SortedEntrySet<CharClass, ImmutableList<T>> splitNext(
            SortedEntrySet<CharClass, ImmutableList<T>> listEntries,
            SortedEntrySet.Entry<CharClass, T> nextEntry) {
        SortedEntrySet<CharClass, ImmutableList<T>> result = new SortedEntrySet<>();
        for (SortedEntrySet.Entry<CharClass, ImmutableList<T>> listEntry : listEntries) {
            CharClass previousCharClass = listEntry.key();
            ImmutableList<T> previousValues = listEntry.value();
            if (nextEntry == null) {
                result.add(previousCharClass, previousValues);
                continue;
            }
            CharClass nextCharClass = nextEntry.key();
            T nextValue = nextEntry.value();
            SortedEntrySet<CharClass, Containment> headEntries = CharClassSplitter.of(previousCharClass, nextCharClass).split();
            SortedEntrySet.Entry<CharClass, Containment> tailEntry = headEntries.last();
            if (tailEntry.value() == Containment.RIGHT) {
                headEntries.removeLast();
                nextEntry = SortedEntrySet.Entry.of(tailEntry.key(), nextValue);
            } else {
                nextEntry = null;
            }
            for (SortedEntrySet.Entry<CharClass, Containment> headEntry : headEntries) {
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
