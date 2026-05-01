package hu.webarticum.holodb.admin.util;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.ImmutableMap;

public class NameTransformer {

    public static final String HELP_TEXT =
            "Templates for renaming:\n" +
            "  Syntax:\n" +
            "    {}                original name\n" +
            "    {ascii}           transformed name\n" +
            "    {ascii|snake|30}  chains transformations\n" +
            "    prefix_{}         adds prefix\n" +
            "    A{}B{ascii}C      reuses the name multiple times\n" +
            "  Supported transformations:\n" +
            "    {lower}    lowercase\n" +
            "    {upper}    uppercase\n" +
            "    {const}    CONSTANT_CASE\n" +
            "    {snake}    snake_case\n" +
            "    {camel}    camelCase\n" +
            "    {pascal}   PascalCase\n" +
            "    {ascii}    ASCII transliteration\n" +
            "    {base64}   Base64 encoding\n" +
            "    {escnum}   prefix leading digit with '_'\n" +
            "    {10}       limit length to 10 (or any number)";

    private enum AlnumType { UPPER, LOWER, DIGIT };

    private static final char ESCAPE_CHAR = '\\';
    private static final char OPEN_CHAR = '{';
    private static final char CLOSE_CHAR = '}';
    private static final char PIPE_CHAR = '|';

    private static final char LEADING_DIGIT_ESCAPE_CHAR = '_';

    private static final String[] CLDR_ASCII_TRANSLITERATIONS = new String[] {
        "¡!", "©(C)", "«<<", "®(R)", "±+/-", "»>>", "¿?", "ÆAE", "ÐD", "×*", "ØO", "ÞTH", "ßss", "æae", "ðd", "÷/", "øo", "þth", "ĐD", "đd",
        "ĦH", "ħh", "ıi", "ĸq", "ŁL", "łl", "ŊN", "ŋn", "ŒO", "œo", "ŦT", "ŧt", "ƀb", "ƁB", "ƂB", "ƃb", "ƇC", "ƈc", "ƉD", "ƊD",
        "ƋD", "ƌd", "ƐE", "ƑF", "ƒf", "ƓG", "ƕh", "ƖI", "ƗI", "ƘK", "ƙk", "ƚl", "ƝN", "ƞn", "ƢO", "ƣo", "ƤP", "ƥp", "ƫt", "ƬT",
        "ƭt", "ƮT", "ƲV", "ƳY", "ƴy", "ƵZ", "ƶz", "ǤG", "ǥg", "ȡd", "ȤZ", "ȥz", "ȴl", "ȵn", "ȶt", "ȷj", "ȸdb", "ȹqp", "ȺA", "ȻC",
        "ȼc", "ȽL", "ȾT", "ȿs", "ɀz", "ɃB", "ɄU", "ɆE", "ɇe", "ɈJ", "ɉj", "ɌR", "ɍr", "ɎY", "ɏy", "ɓb", "ɕc", "ɖd", "ɗd", "ɛe",
        "ɟj", "ɠg", "ɡg", "ɢG", "ɦh", "ɧh", "ɨi", "ɪI", "ɫl", "ɬl", "ɭl", "ɱm", "ɲn", "ɳn", "ɴN", "ɶOE", "ɼr", "ɽr", "ɾr", "ʀR",
        "ʂs", "ʈt", "ʉu", "ʋv", "ʏY", "ʐz", "ʑz", "ʙB", "ʛG", "ʜH", "ʝj", "ʟL", "ʠq", "ʣdz", "ʥdz", "ʦts", "ʪls", "ʫlz", "ʹ'", "ʺ\"",
        "ʻ'", "ʼ'", "ʽ'", "˂<", "˃>", "˄^", "ˆ^", "ˈ'", "ˋ`", "ː:", "˖+", "˗-", "ᴀA", "ᴁAE", "ᴃB", "ᴄC", "ᴅD", "ᴆD", "ᴇE", "ᴊJ",
        "ᴋK", "ᴌL", "ᴍM", "ᴏO", "ᴘP", "ᴛT", "ᴜU", "ᴠV", "ᴡW", "ᴢZ", "ᵫue", "ᵬb", "ᵭd", "ᵮf", "ᵯm", "ᵰn", "ᵱp", "ᵲr", "ᵳr", "ᵴs",
        "ᵵt", "ᵶz", "ᵺth", "ᵻI", "ᵽp", "ᵾU", "ᶀb", "ᶁd", "ᶂf", "ᶃg", "ᶄk", "ᶅl", "ᶆm", "ᶇn", "ᶈp", "ᶉr", "ᶊs", "ᶌv", "ᶍx", "ᶎz",
        "ᶏa", "ᶑd", "ᶒe", "ᶓe", "ᶖi", "ᶙu", "ẜs", "ẝs", "ẞSS", "ỺLL", "ỻll", "ỼV", "ỽv", "ỾY", "ỿy", "‐-", "‒-", "–-", "—-", "―-",
        "‖||", "‘'", "’'", "‚,", "‛'", "“\"", "”\"", "„,,", "‟\"", "′'", "‹<", "›>", "⁅[", "⁆]", "⁎*", "₠CE", "₢Cr", "₣Fr.", "₤L.", "₧Pts",
        "₹Rs", "₺TL", "℗(P)", "℘P", "℞Rx", "←<-", "→->", "↔<->", "−-", "∕/", "∖\\", "∣|", "∥||", "≪<<", "≫>>", "⦅((", "⦆))", "ⱠL", "ⱡl", "ⱢL",
        "ⱣP", "ⱤR", "ⱥa", "ⱦt", "ⱧH", "ⱨh", "ⱩK", "ⱪk", "ⱫZ", "ⱬz", "ⱮM", "ⱱv", "ⱲW", "ⱳw", "ⱴv", "ⱸe", "ⱺo", "ⱾS", "ⱿZ", "、,",
        "。.", "〇0", "〈<", "〉>", "《<<", "》>>", "〔[", "〕]", "〘[", "〙]", "〚[", "〛]", "〝\"", "〞\"", "ꜰF", "ꜱS", "ꜲAA", "ꜳaa", "ꜴAO", "ꜵao",
        "ꜶAU", "ꜷau", "ꜸAV", "ꜹav", "ꜺAV", "ꜻav", "ꜼAY", "ꜽay", "ꝀK", "ꝁk", "ꝂK", "ꝃk", "ꝄK", "ꝅk", "ꝆL", "ꝇl", "ꝈL", "ꝉl", "ꝊO", "ꝋo",
        "ꝌO", "ꝍo", "ꝎOO", "ꝏoo", "ꝐP", "ꝑp", "ꝒP", "ꝓp", "ꝔP", "ꝕp", "ꝖQ", "ꝗq", "ꝘQ", "ꝙq", "ꝞV", "ꝟv", "ꝠVY", "ꝡvy", "ꝤTH", "ꝥth",
        "ꝦTH", "ꝧth", "ꝱd", "ꝲl", "ꝳm", "ꝴn", "ꝵr", "ꝶR", "ꝷt", "ꝹD", "ꝺd", "ꝻF", "ꝼf", "ꞆT", "ꞇt", "ꞐN", "ꞑn", "ꞒC", "ꞓc", "ꞠG",
        "ꞡg", "ꞢK", "ꞣk", "ꞤN", "ꞥn", "ꞦR", "ꞧr", "ꞨS", "ꞩs", "ꞪH",
    };

    private static final ImmutableMap<String, Function<String, String>> TRANSFORMS = ImmutableMap.of(
            "lower", String::toLowerCase,
            "upper", String::toUpperCase,
            "const", NameTransformer::toConstCase,
            "snake", NameTransformer::toSnakeCase,
            "camel", NameTransformer::toCamelCase,
            "pascal", NameTransformer::toPascalCase,
            "ascii", NameTransformer::toAsciiPrintable,
            "base64", NameTransformer::toBase64,
            "escnum", NameTransformer::escapeLeadingDigit);

    private static final java.util.Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final ImmutableList<Function<String, String>> parts;

    private NameTransformer(ImmutableList<Function<String, String>> parts) {
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

    private static String toConstCase(String name) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (String token : tokenize(name)) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append('_');
            }
            resultBuilder.append(token.toUpperCase());
        }
        return resultBuilder.toString();
    }

    private static String toSnakeCase(String name) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (String token : tokenize(name)) {
            if (first) {
                first = false;
            } else {
                resultBuilder.append('_');
            }
            resultBuilder.append(token.toLowerCase());
        }
        return resultBuilder.toString();
    }

    private static String toCamelCase(String name) {
        StringBuilder resultBuilder = new StringBuilder();
        boolean first = true;
        for (String token : tokenize(name)) {
            if (first) {
                resultBuilder.append(token.toLowerCase());
                first = false;
            } else {
                resultBuilder.append(Character.toUpperCase(token.charAt(0)));
                resultBuilder.append(token.substring(1).toLowerCase());
            }
        }
        return resultBuilder.toString();
    }

    private static String toPascalCase(String name) {
        StringBuilder resultBuilder = new StringBuilder();
        for (String token : tokenize(name)) {
            resultBuilder.append(Character.toUpperCase(token.charAt(0)));
            resultBuilder.append(token.substring(1).toLowerCase());
        }
        return resultBuilder.toString();
    }

    private static List<String> tokenize(String name) {
        if (hasNonAlnum(name)) {
            return splitByNonAlnum(name);
        } else {
            return splitAlnumOnly(name);
        }
    }

    private static boolean hasNonAlnum(String name) {
        int length = name.length();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> splitByNonAlnum(String name) {
        List<String> result = new ArrayList<>();
        int length = name.length();
        StringBuilder currentTokenBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                currentTokenBuilder.append(c);
            } else if (!currentTokenBuilder.isEmpty()) {
                result.add(currentTokenBuilder.toString());
                currentTokenBuilder.setLength(0);
            }
        }
        if (!currentTokenBuilder.isEmpty()) {
            result.add(currentTokenBuilder.toString());
        }
        return result;
    }

    private static List<String> splitAlnumOnly(String name) {
        List<String> result = new ArrayList<>();
        int length = name.length();
        StringBuilder currentTokenBuilder = new StringBuilder();
        AlnumType previousType = null;
        for (int i = 0; i < length; i++) {
            char c = name.charAt(i);
            if (Character.isDigit(c)) {
                if (previousType != AlnumType.DIGIT && !currentTokenBuilder.isEmpty()) {
                    result.add(currentTokenBuilder.toString());
                    currentTokenBuilder.setLength(0);
                }
                currentTokenBuilder.append(c);
                previousType = AlnumType.DIGIT;
            } else if (Character.isLowerCase(c)) {
                if (previousType == AlnumType.DIGIT && !currentTokenBuilder.isEmpty()) {
                    result.add(currentTokenBuilder.toString());
                    currentTokenBuilder.setLength(0);
                }
                currentTokenBuilder.append(c);
                previousType = AlnumType.LOWER;
            } else {
                if (!currentTokenBuilder.isEmpty() && checkTitleChar(name, i, previousType)) {
                    result.add(currentTokenBuilder.toString());
                    currentTokenBuilder.setLength(0);
                }
                currentTokenBuilder.append(c);
                previousType = AlnumType.UPPER;
            }
        }
        if (!currentTokenBuilder.isEmpty()) {
            result.add(currentTokenBuilder.toString());
        }
        return result;
    }

    private static boolean checkTitleChar(String name, int i, AlnumType previousType) {
        if (previousType != AlnumType.UPPER) {
            return true;
        } else if (i == name.length() - 1) {
            return false;
        } else {
            char next = name.charAt(i + 1);
            return !Character.isDigit(next) && Character.isLowerCase(next);
        }
    }

    private static String toAsciiPrintable(String name) {
        String decomposed = Normalizer.normalize(name, Normalizer.Form.NFKD);
        int decomposedLength = decomposed.length();
        StringBuilder resultBuilder = new StringBuilder(decomposedLength);
        for (int i = 0; i < decomposedLength; i++) {
            char c = decomposed.charAt(i);
            if ((c - 32) < 95) {
                resultBuilder.append(c);
            } else {
                appendTransliterationIfExists(resultBuilder, c);
            }
        }
        return resultBuilder.toString();
    }

    private static void appendTransliterationIfExists(StringBuilder resultBuilder, char c) {
        int low = 0;
        int high = CLDR_ASCII_TRANSLITERATIONS.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            String item = CLDR_ASCII_TRANSLITERATIONS[mid];
            char itemChar = item.charAt(0);
            if (itemChar < c) {
                low = mid + 1;
            } else if (itemChar > c) {
                high = mid - 1;
            } else {
                resultBuilder.append(item.substring(1));
                return;
            }
        }
    }

    private static String toBase64(String name) {
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        return BASE64_ENCODER.encodeToString(bytes);
    }

    private static String escapeLeadingDigit(String name) {
        if (name.isEmpty()) {
            return "";
        }
        char c = name.charAt(0);
        if (c >= '0' && c <= '9') {
            return LEADING_DIGIT_ESCAPE_CHAR + name;
        } else {
            return name;
        }
    }

    public String transform(String original) {
        StringBuilder resultBuilder = new StringBuilder();
        for (Function<String, String> part : parts) {
            resultBuilder.append(part.apply(original));
        }
        return resultBuilder.toString();
    }

}
