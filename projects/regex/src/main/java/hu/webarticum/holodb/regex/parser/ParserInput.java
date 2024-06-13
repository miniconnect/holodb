package hu.webarticum.holodb.regex.parser;

import java.util.NoSuchElementException;

public class ParserInput {
    
    private final char[] characters;
    
    private int position = 0;
    
    
    public ParserInput(String pattern) {
        this.characters = pattern.toCharArray();
    }
    

    public int position() {
        return position;
    }
    
    public boolean hasNext() {
        return position < characters.length;
    }

    public char next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no more characters");
        }
        char next = characters[position];
        position++;
        return next;
    }
    
    public void storno() {
        if (position == 0) {
            throw new NoSuchElementException("Already at the start position");
        }
        position--;
    }

}
