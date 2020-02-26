package hu.webarticum.holodb.util_old.path;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import hu.webarticum.holodb.util_old.path.Path;
import hu.webarticum.holodb.util_old.path.PathStringParser;

class PathStringParserTest {

    private PathStringParser parser;
    
    
    @Test
    void testEmpty() {
        assertThat(parser.parse("")).isEqualTo(Path.of(""));
    }

    @Test
    void testSingle() {
        assertThat(parser.parse("lorem")).isEqualTo(Path.of("lorem"));
    }

    @Test
    void testBracket() {
        assertThat(parser.parse("lorem[3]")).isEqualTo(Path.of("lorem", 3L));
    }

    @Test
    void testDot() {
        assertThat(parser.parse("lorem.ipsum")).isEqualTo(Path.of("lorem", "ipsum"));
    }

    @Test
    void testQuote() {
        assertThat(parser.parse("lorem.\"ipsum\"")).isEqualTo(Path.of("lorem", "ipsum"));
    }

    @Test
    void testQuoteInBracket() {
        assertThat(parser.parse("lorem[\"ipsum\"]")).isEqualTo(Path.of("lorem", "ipsum"));
    }

    @Test
    void testComplex() {
        assertThat(parser.parse("xxx.yyy.44[3][lorem][\"ipsum\"].\"vv\\\\v\\\"\"[23][\"23\"].x[\"\"]"))
                .isEqualTo(Path.of(
                        "xxx", "yyy", 44L, 3L, "lorem", "ipsum", "vv\\v\"", 23L, "23", "x", ""));
    }
    
    @BeforeEach
    void init() {
        parser = new PathStringParser();
    }

}
