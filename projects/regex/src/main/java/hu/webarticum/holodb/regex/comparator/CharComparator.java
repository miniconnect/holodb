package hu.webarticum.holodb.regex.comparator;

@FunctionalInterface
public interface CharComparator {

    public int compare(char a, char b);
    
}
