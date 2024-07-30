package hu.webarticum.holodb.regex.graph;

import java.util.Comparator;

public class GraphValueComparator implements Comparator<Object> {
    
    private static final GraphValueComparator INSTANCE = new GraphValueComparator();

    private GraphValueComparator() {
        // use instance() instead
    }
    
    public static GraphValueComparator instance() {
        return INSTANCE;
    }

    @Override
    public int compare(Object value1, Object value2) {
        if (value1 instanceof SpecialValue) {
            if (!(value2 instanceof SpecialValue)) {
                return -1;
            } else if (value1 == value2) {
                
            }
            boolean value2FallsBefore = ((SpecialValue) value1).ordinal() > ((SpecialValue) value2).ordinal();
            return value2FallsBefore ? 1 : -1;
        } else if (value1 instanceof CharacterValue) {
            if (!(value2 instanceof CharacterValue)) {
                return 1;
            }
            char char1 = ((CharacterValue) value1).value();
            char char2 = ((CharacterValue) value2).value();
            return compareCharacters(char1, char2);
        } else {
            throw new IllegalArgumentException("Invalid value for comparison: " + value1);
        }
    }
    
    // FIXME
    public static int compareCharacters(char char1, char char2) {
        return Character.compare(char1, char2);
    }
    
}
