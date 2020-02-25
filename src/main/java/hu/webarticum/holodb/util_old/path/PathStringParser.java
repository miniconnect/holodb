package hu.webarticum.holodb.util_old.path;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathStringParser {

    private static final Pattern tokenPttern = Pattern.compile(
            "(?:^|[\\.\\[])(?:([0-9]+)|\"((?:\\\\.|[^\"])*)\"|([^\\.\\[\\]]*))(?:\\]?)");

    private static final Pattern unescapePattern = Pattern.compile("\\\\(.)");
    
    
    public Path parse(String pathString) {
        List<PathEntry> entries = new ArrayList<>();
        
        Matcher matcher = tokenPttern.matcher(pathString);
        while (matcher.find()) {
            String index = matcher.group(1);
            String quotedName = matcher.group(2);
            String name = matcher.group(3);

            PathEntry entry;
            if (index != null) {
                entry = new PathEntry(parseBigInteger(index));
            } else if (quotedName != null) {
                entry = new PathEntry(unescapePattern.matcher(quotedName).replaceAll("$1"));
            } else if (name != null) {
                entry = new PathEntry(name);
            } else {
                throw new IllegalArgumentException("Unexpected error during parsing");
            }
            entries.add(entry);
        }
        
        return new Path(entries);
    }
    
    private static BigInteger parseBigInteger(String numeric) {
        if (numeric.length() <= 5) {
            return BigInteger.valueOf(Long.parseLong(numeric));
        } else {
            return new BigInteger(numeric);
        }
    }

}
