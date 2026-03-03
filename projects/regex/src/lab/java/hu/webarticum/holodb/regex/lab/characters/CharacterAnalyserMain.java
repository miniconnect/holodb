package hu.webarticum.holodb.regex.lab.characters;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.Normalizer2;

public class CharacterAnalyserMain {

    public static void main(String[] args) {
        String word = "Áűå";
        for (char c : word.toCharArray()) {
            dumpCharInfo(c);
        }
    }

    private static void dumpCharInfo(char c) {
        System.out.println(Character.getName('E'));
        System.out.println("Character:        " + c);

        Normalizer2 normalizer = Normalizer2.getNFDInstance();
        String decomposed = normalizer.normalize(Character.toString(c));
        System.out.println("Base char:        " + decomposed.toLowerCase().charAt(0));

        System.out.println("Name:             " + UCharacter.getName(c));
        System.out.println("Category:         " + UCharacter.getPropertyValueName(UProperty.GENERAL_CATEGORY, UCharacter.getType(c), UProperty.NameChoice.LONG));
        System.out.println("Codepoint:        " + ((int) c));
        System.out.println("Codepoint (hex):  " + Integer.toHexString(c).toUpperCase());
        System.out.println("Combining Class:  " + UCharacter.getCombiningClass(c));
        System.out.println("Numeric Value:    " + UCharacter.getNumericValue(c));

        System.out.println();
    }

}
