package hu.webarticum.holodb.bootstrap.misc;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hu.webarticum.holodb.config.HoloConfigColumn.DummyTextKind;
import hu.webarticum.holodb.core.data.random.TreeRandom;
import hu.webarticum.holodb.core.data.source.Source;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

public class DummyTextSource implements Source<String> {

    private static final String[] LOREM_WORDS = {
        "adipiscing", "aliqua", "aliquip", "amet", "anim", "aute", "cillum", "commodo", "consectetur",
        "consequat", "culpa", "cupidatat", "deserunt", "dolor", "dolore", "duis", "eiusmod", "elit",
        "enim", "esse", "est", "excepteur", "exercitation", "fugiat", "incididunt", "ipsum", "irure",
        "labore", "laboris", "laborum", "lorem", "magna", "minim", "mollit", "nisi", "nostrud", "nulla",
        "occaecat", "officia", "pariatu", "proident", "qui", "quis", "reprehenderit", "sed", "sint",
        "sit", "sunt", "tempor", "ullamco", "velit", "veniam", "voluptate",
    };

    private static final String[] CONJUNCTION_WORDS = { "and", "for", "if", "of", "or", "the" };

    private static final String[] PRIMARY_PRE_WORDS = { "a", "the" };

    private static final String[] SECONDARY_PRE_WORDS = { "more", "no", "some", "two" };

    private static final Pattern WRONG_PRE_PATTERN = Pattern.compile("\\b([aA]) (?=[aeiouAEIOU])");


    private final DummyTextKind kind;

    private final TreeRandom treeRandom;

    private final LargeInteger size;


    public DummyTextSource(DummyTextKind kind, TreeRandom treeRandom, LargeInteger size) {
        this.kind = kind;
        this.treeRandom = treeRandom;
        this.size = size;
    }


    @Override
    public Class<?> type() {
        return String.class;
    }

    @Override
    public LargeInteger size() {
        return size;
    }

    @Override
    public String get(LargeInteger index) {
        int seed = treeRandom.sub(index).getNumber(LargeInteger.of(Integer.MAX_VALUE)).intValue();
        Random random = new Random(seed);
        switch (kind) {
            case PHRASE:
                return shortPhrase(random, false);
            case TITLE:
                return title(random);
            case SENTENCE:
                return sentence(random);
            case PARAGRAPH:
                return paragraph(random);
            case MARKDOWN:
                return markdown(random);
            case HTML:
                return html(random);
            default:
                return "";
        }
    }

    @Override
    public Optional<ImmutableList<String>> possibleValues() {
        return Optional.empty();
    }

    private static String html(Random random) {
        StringBuilder resultBuilder = new StringBuilder("<h1>");
        resultBuilder.append(title(random));
        resultBuilder.append("</h1>");
        int sectionCount = 1 + random.nextInt(3);
        for (int i = 0; i < sectionCount; i++) {
            resultBuilder.append("\n\n<h2>");
            resultBuilder.append(title(random));
            resultBuilder.append("</h2>");
            int paragraphCount = 1 + random.nextInt(2);
            for (int j = 0; j < paragraphCount; j++) {
                resultBuilder.append("\n\n<p>");
                int sentenceCount = 3 + random.nextInt(3);
                for (int k = 0; k < sentenceCount; k++) {
                    resultBuilder.append("\n  ");
                    resultBuilder.append(sentence(random));
                }
                resultBuilder.append("\n</p>");
            }
        }
        return resultBuilder.toString();
    }

    private static String markdown(Random random) {
        StringBuilder resultBuilder = new StringBuilder("# ");
        resultBuilder.append(title(random));
        int sectionCount = 1 + random.nextInt(3);
        for (int i = 0; i < sectionCount; i++) {
            resultBuilder.append("\n\n## ");
            resultBuilder.append(title(random));
            int paragraphCount = 1 + random.nextInt(2);
            for (int j = 0; j < paragraphCount; j++) {
                resultBuilder.append("\n");
                int sentenceCount = 3 + random.nextInt(3);
                for (int k = 0; k < sentenceCount; k++) {
                    resultBuilder.append("\n");
                    resultBuilder.append(sentence(random));
                }
            }
        }
        return resultBuilder.toString();
    }

    private static String paragraph(Random random) {
        int sentenceCount = 3 + random.nextInt(3);
        StringBuilder resultBuilder = new StringBuilder(sentence(random));
        for (int i = 1; i < sentenceCount; i++) {
            resultBuilder.append(' ');
            resultBuilder.append(sentence(random));
        }
        return resultBuilder.toString();
    }

    private static String sentence(Random random) {
        return upperFirst(longPhrase(random, false)) + ".";
    }

    private static String title(Random random) {
        return upperFirst(shortPhrase(random, true));
    }

    private static String longPhrase(Random random, boolean capitalize) {
        return phrase(random, 5, 12, capitalize);
    }

    private static String shortPhrase(Random random, boolean capitalize) {
        return phrase(random, 3, 6, capitalize);
    }

    private static String phrase(Random random, int minWordCount, int maxWordCount, boolean capitalize) {
        String rawPhrase = rawPhrase(random, minWordCount, maxWordCount, capitalize);
        Matcher matcher = WRONG_PRE_PATTERN.matcher(rawPhrase);
        StringBuffer resultBuffer = null;
        boolean wasModified = false;
        while (matcher.find()) {
            if (resultBuffer == null) {
                resultBuffer = new StringBuffer();
            }
            matcher.appendReplacement(resultBuffer, matcher.group(1) + "n ");
            wasModified = true;
        }
        if (!wasModified) {
            return rawPhrase;
        }
        matcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }

    private static String rawPhrase(Random random, int minWordCount, int maxWordCount, boolean capitalize) {
        int wordCount = (minWordCount < maxWordCount) ? minWordCount + random.nextInt(maxWordCount - minWordCount) : minWordCount;
        int previousType = 1;
        StringBuilder resultBuilder = new StringBuilder();
        for (int i = 1; i < wordCount; i++) {
            int choice = random.nextInt(2);
            if (previousType < 2 && choice == 0) {
                resultBuilder.append(preWord(random));
                previousType = 2;
            } else if (previousType == 0 && choice == 0) {
                resultBuilder.append(conjunctionWord(random));
                previousType = 1;
            } else {
                resultBuilder.append(loremWord(random, capitalize));
                previousType = 0;
            }
            resultBuilder.append(' ');
        }
        resultBuilder.append(loremWord(random, capitalize));
        return resultBuilder.toString();
    }

    private static String preWord(Random random) {
        String[] preWords = (random.nextInt(4) == 0) ? SECONDARY_PRE_WORDS : PRIMARY_PRE_WORDS;
        return preWords[random.nextInt(preWords.length)];
    }

    private static String conjunctionWord(Random random) {
        return CONJUNCTION_WORDS[random.nextInt(CONJUNCTION_WORDS.length)];
    }

    private static String loremWord(Random random, boolean capitalize) {
        String word = LOREM_WORDS[random.nextInt(LOREM_WORDS.length)];
        if (capitalize) {
            word = upperFirst(word);
        }
        return word;
    }

    private static String upperFirst(String word) {
        char firstChar = Character.toUpperCase(word.charAt(0));
        String remaining = word.substring(1);
        return firstChar + remaining;
    }

}
