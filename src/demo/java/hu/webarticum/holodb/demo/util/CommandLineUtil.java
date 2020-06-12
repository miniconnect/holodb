package hu.webarticum.holodb.demo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class CommandLineUtil {

    public static <T> Pair<Integer, T> readOption(String message, List<Pair<String, T>> options) {
        int optionCount = options.size();
        StringBuilder fullMessageBuilder = new StringBuilder(message);
        fullMessageBuilder.append("\n\n");
        for (int i = 0; i < optionCount; i++) {
            fullMessageBuilder.append("  ");
            fullMessageBuilder.append(i);
            fullMessageBuilder.append(") ");
            fullMessageBuilder.append(options.get(i).getLeft());
            fullMessageBuilder.append('\n');
        }
        fullMessageBuilder.append('\n');
        fullMessageBuilder.append("Choose an option from the above list");
        String fullMessage = fullMessageBuilder.toString();
        int selectedIndex = readIntBetween(fullMessage, 0, optionCount);
        T selectedValue = options.get(selectedIndex).getRight();
        return Pair.of(selectedIndex, selectedValue);
    }

    public static int readInt(String message) {
        return readData(message, Integer::parseInt);
    }

    public static int readIntAtLeast(String message, int min) {
        return readIntBetween(message, min, Integer.MAX_VALUE);
    }
    
    public static int readIntBetween(String message, int min, int highExclusive) {
        String fullMessage;
        if (highExclusive == Integer.MAX_VALUE) {
            fullMessage = String.format("%s (>= %d)", message, min);
        } else {
            fullMessage = String.format("%s %d..%d", message, min, highExclusive - 1);
        }
        return readData(fullMessage, value -> {
            int number = Integer.parseInt(value);
            if (number < min || number >= highExclusive) {
                throw new IllegalArgumentException(String.format(
                        "Value must be in the range [%d..%d)", min, highExclusive));
            }
            return number;
        });
    }

    public static long readLong(String message) {
        return readData(message, Long::parseLong);
    }

    public static BigInteger readBigInteger(String message) {
        return readData(message, BigInteger::new);
    }

    public static String readString(String message) {
        return readData(message, value -> value);
    }

    public static <T> T readData(String message, Converter<T> converter) {
        T data = null;
        while (data == null) {
            System.out.print(String.format("%s: ", message)); // NOSONAR
            String value = null;
            try {
                value = readLine();
            } catch (Exception e) {
                System.err.println("Unknown error while reading user input"); // NOSONAR
                e.printStackTrace(); // NOSONAR
            }
            
            if (value == null) {
                continue;
            }
            
            try {
                data = converter.convert(value);
            } catch (ArithmeticException | IllegalArgumentException e) {
                System.out.println(e.getMessage()); // NOSONAR
            } catch (Exception e) {
                System.out.println("Unknown error while interpreting user input"); // NOSONAR
                e.printStackTrace(); // NOSONAR
            }
        }
        return data;
    }
    
    public static String readLine() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    public static void printTitle(String title) {
        System.out.println(); // NOSONAR
        printRepeated('=', title.length() + 6);
        System.out.println(); // NOSONAR
        System.out.println(String.format("#  %s  #", title)); // NOSONAR
        printRepeated('=', title.length() + 6);
        System.out.println(); // NOSONAR
        System.out.println(); // NOSONAR
    }
    
    public static void printSeparator() {
        System.out.println(); // NOSONAR
        printRepeated('=', 100);
        System.out.println(); // NOSONAR
        System.out.println(); // NOSONAR
    }

    public static void printRepeated(char c, int width) {
        for (int i = 0; i < width; i++) {
            System.out.print(c); // NOSONAR
        }
    }
    
    
    public interface Converter<T> {
        
        public T convert(String value) throws Exception;
        
    }
    
}
