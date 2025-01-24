package hu.webarticum.holodb.regex.NEW.charclass;

import java.util.ArrayList;
import java.util.List;

public class CharClassSplitter {

    public enum Containment { LEFT, RIGHT, BOTH }
    
    private final CharClass leftCharClass;

    private final CharClass rightCharClass;
    
    private CharClassSplitter(CharClass leftCharClass, CharClass rightCharClass) {
        this.leftCharClass = leftCharClass;
        this.rightCharClass = rightCharClass;
    }

    public static CharClassSplitter of(CharClass leftCharClass, CharClass rightCharClass) {
        return new CharClassSplitter(leftCharClass, rightCharClass);
    }
    
    public SortedEntrySet<CharClass, Containment> split() {
        SortedEntrySet<CharClass, Containment> result = new SortedEntrySet<>();
        CharComparator comparator = leftCharClass.charComparator();
        Containment currentContainment = Containment.LEFT;
        List<Character> currentBuilder = new ArrayList<>();
        String leftCharacters = leftCharClass.chars();
        String rightCharacters = rightCharClass.chars();
        int leftSize = leftCharacters.length();
        int rightSize = rightCharacters.length();
        int leftPos = 0;
        int rightPos = 0;
        while (leftPos < leftSize) {
            char left = leftCharacters.charAt(leftPos);
            while (rightPos < rightSize) {
                char right = rightCharacters.charAt(rightPos);
                if (comparator.compare(left, right) > 0) {
                    currentBuilder = flushBuilder(result, currentBuilder, currentContainment, Containment.RIGHT);
                    currentContainment = Containment.RIGHT;
                    currentBuilder.add(right);
                    rightPos++;
                } else {
                    break;
                }
            }
            boolean wasBoth = false;
            if (rightPos < rightSize) {
                char right = rightCharacters.charAt(rightPos);
                if (comparator.compare(left, right) == 0) {
                    currentBuilder = flushBuilder(result, currentBuilder, currentContainment, Containment.BOTH);
                    currentContainment = Containment.BOTH;
                    currentBuilder.add(left);
                    leftPos++;
                    rightPos++;
                    wasBoth = true;
                }
            }
            if (!wasBoth) {
                currentBuilder = flushBuilder(result, currentBuilder, currentContainment, Containment.LEFT);
                currentContainment = Containment.LEFT;
                currentBuilder.add(left);
                leftPos++;
            }
        }
        while (rightPos < rightSize) {
            char right = rightCharacters.charAt(rightPos);
            currentBuilder = flushBuilder(result, currentBuilder, currentContainment, Containment.RIGHT);
            currentContainment = Containment.RIGHT;
            currentBuilder.add(right);
            rightPos++;
        }
        flushBuilder(result, currentBuilder, currentContainment, null);
        return result;
    }
    
    private List<Character> flushBuilder(
            SortedEntrySet<CharClass, Containment> result, List<Character> currentBuilder, Containment currentContainment, Containment newContainment) {
        if (newContainment != currentContainment && !currentBuilder.isEmpty()) {
            CharClass charClass = CharClass.of(characterListToString(currentBuilder), leftCharClass.charComparator());
            result.add(charClass, currentContainment);
            return new ArrayList<>();
        } else {
            return currentBuilder;
        }
    }
    
    private String characterListToString(List<Character> characterList) {
        int length = characterList.size();
        char[] chars = new char[length];
        int i = 0;
        for (Character c : characterList) {
            chars[i] = c;
            i++;
        }
        return String.valueOf(chars);
    }
    
}
