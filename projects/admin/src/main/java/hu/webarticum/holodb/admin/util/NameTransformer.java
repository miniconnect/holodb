package hu.webarticum.holodb.admin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NameTransformer {

    private static final char ESCAPE_CHAR = '\\';
    private static final char OPEN_CHAR = '{';
    private static final char CLOSE_CHAR = '}';
    private static final char PIPE_CHAR = '|';

    // TODO: ascii, snake, camel, pascal
    private static final ImmutableMap<String, Function<String, String>> TRANSFORMS = ImmutableMap.of(
            "lower", String::toLowerCase,
            "upper", String::toUpperCase);

    private final ImmutableList<Function<String, String>> parts;

    public NameTransformer(ImmutableList<Function<String, String>> parts) {
        this.parts = parts;
    }

    public static NameTransformer parse(String transformerPattern) {
        int length = transformerPattern.length();
        int pos = 0;
        List<Function<String, String>> partsBuilder = new ArrayList<>();
        StringBuilder currentLiteralBuilder = new StringBuilder();
        while (pos < length) {
            char nextChar = transformerPattern.charAt(pos);
            if (nextChar == ESCAPE_CHAR) {
                pos++;
                if (pos < length) {
                    currentLiteralBuilder.append(transformerPattern.charAt(pos));
                }
                pos++;
            } else if (nextChar == OPEN_CHAR) {
                if (!currentLiteralBuilder.isEmpty()) {
                    String literal = currentLiteralBuilder.toString();
                    partsBuilder.add(s -> literal);
                    currentLiteralBuilder.setLength(0);
                }
                pos = parseSubstitution(transformerPattern, pos + 1, partsBuilder);
            } else {
                currentLiteralBuilder.append(nextChar);
                pos++;
            }
        }
        if (!currentLiteralBuilder.isEmpty()) {
            String finalLiteral = currentLiteralBuilder.toString();
            partsBuilder.add(s -> finalLiteral);
        }
        return new NameTransformer(ImmutableList.fromCollection(partsBuilder));
    }

    private static int parseSubstitution(
            String transformerPattern, int pos, List<Function<String, String>> partsBuilder) {
        int length = transformerPattern.length();
        Function<String, String> function = null;
        StringBuilder functionNameBuilder = new StringBuilder();
        while (pos < length) {
            char nextChar = transformerPattern.charAt(pos);
            if (nextChar == ESCAPE_CHAR) {
                pos++;
                if (pos < length) {
                    functionNameBuilder.append(transformerPattern.charAt(pos));
                }
                pos++;
            } else if (nextChar == PIPE_CHAR) {
                function = chainFunction(function, functionNameBuilder.toString());
                functionNameBuilder.setLength(0);
                pos++;
            } else if (nextChar == CLOSE_CHAR) {
                pos++;
                break;
            } else {
                functionNameBuilder.append(transformerPattern.charAt(pos));
                pos++;
            }
        }
        partsBuilder.add(chainFunction(function, functionNameBuilder.toString()));
        return pos;
    }

    private static Function<String, String> chainFunction(Function<String, String> current, String nameBuffer) {
        if (!nameBuffer.isEmpty()) {
            Function<String, String> next = lookUpFunction(nameBuffer);
            return current != null ? current.andThen(next) : next;
        } else {
            return current != null ? current : t -> t;
        }
    }

    private static Function<String, String> lookUpFunction(String name) {
        Function<String, String> predefined = TRANSFORMS.get(name);
        if (predefined != null) {
            return predefined;
        } else if (name.chars().allMatch(Character::isDigit)) {
            int max = Integer.parseInt(name);
            return t -> limitLength(t, max);
        } else {
            throw new IllegalArgumentException("Unknown transformer function: " + name);
        }
    }

    private static String limitLength(String name, int max) {
        int length = name.length();
        return length > max ? name.substring(0, max) : name;
    }

    public String transform(String original) {
        StringBuilder resultBuilder = new StringBuilder();
        for (Function<String, String> part : parts) {
            resultBuilder.append(part.apply(original));
        }
        return resultBuilder.toString();
    }

}
