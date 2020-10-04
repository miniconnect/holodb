package hu.webarticum.holodb.ng.simple;

import java.math.BigInteger;

public class SimpleTestMain {

    public static void main(String[] args) {
        
        // TODO
        
        AutomatonSource source = new AutomatonSource("[0-9]{10}", 30);
        for (
                BigInteger i = BigInteger.ZERO;
                i.compareTo(source.size()) <= 0;
                i = i.add(BigInteger.ONE)) {
            System.out.println(source.get(i));
        }
        
    }
    
}
