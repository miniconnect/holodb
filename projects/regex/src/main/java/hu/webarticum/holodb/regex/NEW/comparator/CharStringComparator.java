package hu.webarticum.holodb.regex.NEW.comparator;

import java.util.Comparator;

public class CharStringComparator implements Comparator<String> {

    private final CharComparator charComparator;
    
    public CharStringComparator(CharComparator charComparator) {
        this.charComparator = charComparator;
    }

    @Override
    public int compare(String string1, String string2) {
        int length1 = string1.length();
        int length2 = string2.length();
        int commonLength = length1 < length2 ? length1 : length2;
        for (int i = 0; i < commonLength; i++) {
            char c1 = string1.charAt(i);
            char c2 = string2.charAt(i);
            int cmp = charComparator.compare(c1, c2);
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(length1, length2);
    }
    
}
