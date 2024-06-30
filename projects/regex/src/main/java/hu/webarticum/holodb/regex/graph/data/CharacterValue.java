package hu.webarticum.holodb.regex.graph.data;

// TODO: more possibilities, references etc.
public class CharacterValue implements Comparable<CharacterValue> {

    private final char value;
    
    public CharacterValue(char value) {
        this.value = value;
    }
    
    public char value() {
        return value;
    }

    @Override
    public int compareTo(CharacterValue other) {
        return Character.compare(value, other.value);
    }
    
    @Override
    public int hashCode() {
        return Character.hashCode(value);
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof CharacterValue)) {
            return false;
        } else {
            return value == ((CharacterValue) other).value;
        }
    }
    
    @Override
    public String toString() {
        return "'" + (Character.isISOControl(value) ? "\\u" + Integer.toHexString(value) : value) + "'";
    }
    
}
