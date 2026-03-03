package hu.webarticum.holodb.regex.comparator;

import java.text.Normalizer;
import java.util.Arrays;

public class DefaultCharComparator implements CharComparator {

    // TODO: we could hard-code the tables later
    private static final String MOST_USED_COMPOSED_LETTERS = "áäâàåéëêèíïîìóöõőôòøúüũűûùçñß";

    private static final char[] LETTER_CACHE_LOOKUP;

    private static final char[] LETTER_CACHE_VALUES;

    static {
        int length = MOST_USED_COMPOSED_LETTERS.length();
        int[] codepoints = new int[length];
        for (int i = 0; i < length; i++) {
            codepoints[i] = MOST_USED_COMPOSED_LETTERS.charAt(i);
        }
        Arrays.sort(codepoints);
        LETTER_CACHE_LOOKUP = new char[length];
        LETTER_CACHE_VALUES = new char[length];
        for (int i = 0; i < length; i++) {
            char c = (char) codepoints[i];
            LETTER_CACHE_LOOKUP[i] = c;
            LETTER_CACHE_VALUES[i] = baseCharOf(c);
        }
    }

    private static char baseCharOf(char c) {
        if (c >= 'a' && c <= 'z') {
            return c;
        } else {
            return Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD).charAt(0);
        }
    }

    @Override
    public int compare(char a, char b) {
        if (a == b) {
            return 0;
        } else if (Character.isDigit(a)) {
            return compareDigit(a, b);
        } else if (Character.isAlphabetic(a)) {
            return compareAlphabetic(a, b);
        } else {
            return compareOther(a, b);
        }
    }

    private int compareOther(char a, char b) {
        if (Character.isDigit(b) || Character.isAlphabetic(b)) {
            return -1;
        } else {
            return Character.compare(a, b);
        }
    }

    private int compareDigit(char a, char b) {
        if (Character.isDigit(b)) {
            return Character.compare(a, b);
        } else if (Character.isAlphabetic(b)) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareAlphabetic(char a, char b) {
        if (!Character.isAlphabetic(b)) {
            return 1;
        }

        int rawCmp = Character.compare(a, b);
        if (rawCmp == 0) {
            return 0;
        }

        char lower1 = Character.toLowerCase(a);
        char lower2 = Character.toLowerCase(b);
        int lowerCmp = Character.compare(lower1, lower2);
        if (lowerCmp == 0) {
            return rawCmp;
        }

        char base1 = baseCharOfCached(lower1);
        if (base1 == lower2) {
            return 1;
        }

        char base2 = baseCharOfCached(lower2);

        int baseCmp = Character.compare(base1, base2);
        if (baseCmp == 0) {
            return lowerCmp;
        }

        return baseCmp;
    }

    private char baseCharOfCached(char c) {
        int foundIndex = Arrays.binarySearch(LETTER_CACHE_LOOKUP, c);
        if (foundIndex >= 0) {
            return LETTER_CACHE_VALUES[foundIndex];
        } else {
            return baseCharOf(c);
        }
    }

}
