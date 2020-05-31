package hu.webarticum.holodb.lab.util;

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

    public static int readIntBetween(String message, int min, int highExclusive) {
        return readData(message, value -> {
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
                System.err.println("Invalid input"); // NOSONAR
            } catch (Exception e) {
                System.err.println("Unknown error while interpreting user input"); // NOSONAR
                e.printStackTrace(); // NOSONAR
            }
        }
        return data;
    }
    
    public static String readLine() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }
    
    public static void printSeparator() {
        System.out.println(); // NOSONAR
        for (int i = 0; i < 100; i++) {
            System.out.print('='); // NOSONAR
        }
        System.out.println(); // NOSONAR
        System.out.println(); // NOSONAR
    }
    
    
    public interface Converter<T> {
        
        public T convert(String value) throws Exception;
        
    }
    
}
