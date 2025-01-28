package hu.webarticum.holodb.regex.charclass;

import java.util.Objects;
import java.util.TreeSet;

import hu.webarticum.holodb.regex.comparator.CharComparator;
import hu.webarticum.miniconnect.lang.ToStringBuilder;

public class CharClass implements Comparable<CharClass> {
    
    private final String chars;
    
    private final CharComparator charComparator;
    
    private CharClass(String chars, CharComparator charComparator) {
        this.chars = chars;
        this.charComparator = charComparator;
    }
    
    public static CharClass of(String chars, CharComparator charComparator) {
        return new CharClass(normalize(chars, charComparator), charComparator);
    }
    
    private static String normalize(String chars, CharComparator charComparator) {
        int length = chars.length();
        if (length == 0) {
            return chars;
        }
        
        boolean isAlreadyNormalized = true;
        char previousChar = chars.charAt(0);
        for (int i = 1; i < length; i++) {
            char c = chars.charAt(i);
            if (charComparator.compare(c, previousChar) <= 0) {
                isAlreadyNormalized = false;
                break;
            }
            previousChar = c;
        }
        if (isAlreadyNormalized) {
            return chars;
        }
        
        TreeSet<Character> characterSet = new TreeSet<>(charComparator::compare);
        for (int i = 0; i < length; i++) {
            characterSet.add(chars.charAt(i));
        }
        StringBuilder resultBuilder = new StringBuilder();
        for (Character c : characterSet) {
            resultBuilder.append((char) c);
        }
        return resultBuilder.toString();
    }

    public int size() {
        return chars.length();
    }

    public String chars() {
        return chars;
    }

    public CharComparator charComparator() {
        return charComparator;
    }
    
    @Override
    public int compareTo(CharClass other) {
        if (this == other) {
            return 0;
        }
        int size = chars.length();
        int otherSize = other.chars.length();
        int commonSize = Math.min(size, otherSize);
        for (int i = 0; i < commonSize; i++) {
            char character = chars.charAt(i);
            char otherCharacter = other.chars.charAt(i);
            int cmp = charComparator.compare(character, otherCharacter);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (size < otherSize) {
            return -1;
        } else if (size > otherSize) {
            return 1;
        } else {
            return 0;
        }
    }
    
    @Override
    public int hashCode() {
        return chars.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof CharClass)) {
            return false;
        }
        CharClass other = (CharClass) obj;
        return
                chars.equals(other.chars) &&
                Objects.equals(charComparator, other.charComparator);
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("characters", chars)
                .add("charComparator", charComparator)
                .build();
    }
    
    // FIXME: we assume the same comparator
    public CharClass union(CharClass other) {
        if (chars.isEmpty()) {
            return other;
        } else if (other.chars.isEmpty()) {
            return this;
        }
        
        int length = chars.length();
        int otherLength = other.chars.length();
        char[] resultChars = new char[length + otherLength];
        int resultLength = 0;
        int i = 0;
        int j = 0;
        while (true) {
            char a = chars.charAt(i);
            char b = other.chars.charAt(j);
            int cmp = charComparator.compare(a, b);
            if (cmp == 0) {
                resultChars[resultLength] = a;
                resultLength++;
                i++;
                j++;
                if (i == length || j == otherLength) {
                    break;
                }
            } else if (cmp < 0) {
                resultChars[resultLength] = a;
                resultLength++;
                i++;
                if (i == length) {
                    resultChars[resultLength] = b;
                    resultLength++;
                    j++;
                    break;
                }
            } else {
                resultChars[resultLength] = b;
                resultLength++;
                j++;
                if (j == otherLength) {
                    resultChars[resultLength] = a;
                    resultLength++;
                    i++;
                    break;
                }
            }
        }
        for ( ; i < length; i++) {
            resultChars[resultLength] = chars.charAt(i);
            resultLength++;
        }
        for ( ; j < otherLength; j++) {
            resultChars[resultLength] = other.chars.charAt(j);
            resultLength++;
        }
        if (resultLength == length) {
            return this;
        } else {
            String newChars = String.valueOf(resultChars, 0, resultLength);
            return new CharClass(newChars, charComparator);
        }
    }

    // FIXME: we assume the same comparator
    public CharClass intersection(CharClass other) {
        int length = chars.length();
        int otherLength = other.chars.length();
        if (
                chars.isEmpty() ||
                other.chars.isEmpty() ||
                charComparator.compare(chars.charAt(0), other.chars.charAt(otherLength - 1)) > 0 ||
                charComparator.compare(chars.charAt(length - 1), other.chars.charAt(0)) < 0) {
            return new CharClass("", charComparator);
        }
        
        char[] resultChars = new char[length + otherLength];
        int resultLength = 0;
        int i = 0;
        int j = 0;
        while (true) {
            char a = chars.charAt(i);
            char b = other.chars.charAt(j);
            int cmp = charComparator.compare(a, b);
            if (cmp == 0) {
                resultChars[resultLength] = a;
                resultLength++;
                i++;
                j++;
                if (i == length || j == otherLength) {
                    break;
                }
            } else if (cmp < 0) {
                i++;
                if (i == length) {
                    break;
                }
            } else {
                j++;
                if (j == otherLength) {
                    break;
                }
            }
        }
        if (length == resultLength) {
            return this;
        } else {
            String newChars = String.valueOf(resultChars, 0, resultLength);
            return new CharClass(newChars, charComparator);
        }
    }

}
