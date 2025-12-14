package hu.webarticum.holodb.regex.parser;

import java.util.NoSuchElementException;

public class ParserInput {

    private final char[] characters;

    private int position = 0;


    public ParserInput(String pattern) {
        this.characters = pattern.toCharArray();
    }


    public String content() {
        return String.valueOf(characters);
    }

    public int position() {
        return position;
    }

    public boolean hasNext() {
        return position < characters.length;
    }

    public char peek() {
        requireNext();
        return characters[position];
    }

    public char next() {
        requireNext();
        char next = characters[position];
        position++;
        return next;
    }

    public boolean expect(char next) {
        if (hasNext() && (characters[position] == next)) {
            position++;
            return true;
        } else {
            return false;
        }
    }

    private void requireNext() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no more characters");
        }
    }

    public void storno() {
        if (position == 0) {
            throw new NoSuchElementException("Already at the start position");
        }
        position--;
    }

}
