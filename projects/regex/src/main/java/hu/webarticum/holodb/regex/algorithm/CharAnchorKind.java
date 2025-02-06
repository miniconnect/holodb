package hu.webarticum.holodb.regex.algorithm;

import java.util.ArrayList;
import java.util.List;

import hu.webarticum.miniconnect.lang.ImmutableList;

public enum CharAnchorKind {
    
    BEGIN {

        @Override
        public boolean accept(char c) {
            return false;
        }
        
    },

    END {

        @Override
        public boolean accept(char c) {
            return false;
        }
        
    },
    
    WORD {

        @Override
        public boolean accept(char c) {
            return c == '_' || Character.isDigit(c) || Character.isAlphabetic(c);
        }
        
    },

    NEWLINE {

        @Override
        public boolean accept(char c) {
            return c == '\n';
        }
        
    },

    OTHER {

        @Override
        public boolean accept(char c) {
            return !WORD.accept(c) && !NEWLINE.accept(c);
        }
        
    },
    
    ;
    
    public abstract boolean accept(char c);

    public static CharAnchorKind of(char c) {
        if (WORD.accept(c)) {
            return WORD;
        } else if (NEWLINE.accept(c)) {
            return NEWLINE;
        } else {
            return OTHER;
        }
    }
    
    public static ImmutableList<String> separate(String chars) {
        int length = chars.length();
        int capacity = length > 16 ? 16 : length;
        StringBuilder othersBuilder = null;
        StringBuilder alnumBuilder = null;
        StringBuilder newLineBuilder = null;
        for (int i = 0; i < length; i++) {
            char c = chars.charAt(i);
            if (WORD.accept(c)) {
                if (alnumBuilder == null) {
                    alnumBuilder = new StringBuilder(capacity);
                }
                alnumBuilder.append(c);
            } else if (NEWLINE.accept(c)) {
                if (newLineBuilder == null) {
                    newLineBuilder = new StringBuilder(1);
                }
                newLineBuilder.append(c);
            } else {
                if (othersBuilder == null) {
                    othersBuilder = new StringBuilder(capacity);
                }
                othersBuilder.append(c);
            }
        }
        List<String> resultBuilder = new ArrayList<>(3);
        if (othersBuilder != null) {
            resultBuilder.add(othersBuilder.toString());
        }
        if (alnumBuilder != null) {
            resultBuilder.add(alnumBuilder.toString());
        }
        if (newLineBuilder != null) {
            resultBuilder.add(newLineBuilder.toString());
        }
        
        if (resultBuilder.isEmpty()) {
            
            // FIXME
            resultBuilder.add("?");
            
        }
        
        return ImmutableList.fromCollection(resultBuilder);
    }
    
}
