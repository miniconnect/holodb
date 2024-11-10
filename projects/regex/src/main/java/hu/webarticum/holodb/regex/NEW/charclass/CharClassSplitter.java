package hu.webarticum.holodb.regex.NEW.charclass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hu.webarticum.holodb.regex.NEW.SortedEntrySet;
import hu.webarticum.miniconnect.lang.ImmutableList;

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
        Comparator<Character> comparator = leftCharClass.characterComparator();
        Containment currentContainment = Containment.LEFT;
        List<Character> currentBuilder = new ArrayList<>();
        ImmutableList<Character> leftCharacters = leftCharClass.characters();
        ImmutableList<Character> rightCharacters = rightCharClass.characters();
        int leftSize = leftCharacters.size();
        int rightSize = rightCharacters.size();
        int leftPos = 0;
        int rightPos = 0;
        while (leftPos < leftSize) {
            Character left = leftCharacters.get(leftPos);
            while (rightPos < rightSize) {
                Character right = rightCharacters.get(rightPos);
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
                Character right = rightCharacters.get(rightPos);
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
            Character right = rightCharacters.get(rightPos);
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
            CharClass charClass = CharClass.of(currentBuilder, leftCharClass.characterComparator());
            result.add(charClass, currentContainment);
            return new ArrayList<>();
        } else {
            return currentBuilder;
        }
    }
    
}
