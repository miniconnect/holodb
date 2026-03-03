package hu.webarticum.holodb.regex.lab.characters;

import com.mifmif.common.regex.Generex;

import hu.webarticum.holodb.regex.facade.MatchList;
import nl.flotsam.xeger.Xeger;

public class MatchListComparisonMain {

    public static void main(String[] args) {
        String pattern = "[0-9]?[agb](lo[rx]e[zm]|lo[zt]e[as])";

        System.out.println("Generex:");
        Generex generex = new Generex(pattern);
        {
            int i = 0;
            for (String generexMatch : generex.getAllMatchedStrings()) {
                System.out.println(i + ": " + generexMatch);
                i++;
            }
        }

        System.out.println();
        System.out.println("MatchList:");
        MatchList matchList = MatchList.of(pattern);
        int size = matchList.size().intValueExact();
        for (int i = 0; i < size; i++) {
            System.out.println(i + ": " + matchList.get(i));
        }

        System.out.println();
        System.out.println("Xeger:");
        Xeger xeger = new Xeger(pattern);
        for (int i = 0; i < size; i++) {
            System.out.println("?: " + xeger.generate());
        }
    }

}
