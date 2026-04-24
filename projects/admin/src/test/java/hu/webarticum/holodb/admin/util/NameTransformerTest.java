package hu.webarticum.holodb.admin.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import org.junit.jupiter.api.Test;

public class NameTransformerTest {

    @Test
    void testSimpleLiteral() {
        assertThat(NameTransformer.parse("").transform("dolor")).isEmpty();
        assertThat(NameTransformer.parse("lorem").transform("dolor")).isEqualTo("lorem");
        assertThat(NameTransformer.parse("lorem_ipsum").transform("dolor")).isEqualTo("lorem_ipsum");
        assertThat(NameTransformer.parse("ház:tűz?néző!").transform("dolor")).isEqualTo("ház:tűz?néző!");
        assertThat(NameTransformer.parse("lorem|ipsum|dolor").transform("dolor")).isEqualTo("lorem|ipsum|dolor");
    }

    @Test
    void testEscapedLiteral() {
        assertThat(NameTransformer.parse("\\\\").transform("dolor")).isEqualTo("\\");
        assertThat(NameTransformer.parse("\\{").transform("dolor")).isEqualTo("{");
        assertThat(NameTransformer.parse("\\a").transform("dolor")).isEqualTo("a");
        assertThat(NameTransformer.parse("lo\\rem\\\\ip\\sum\\{dolor\\|sit").transform("dolor")).isEqualTo("lorem\\ipsum{dolor|sit");
        assertThat(NameTransformer.parse("lorem\\").transform("dolor")).isEqualTo("lorem");
    }

    @Test
    void testSimpleSubstition() {
        assertThat(NameTransformer.parse("{}").transform("dolor")).isEqualTo("dolor");
        assertThat(NameTransformer.parse("lorem_{}").transform("")).isEqualTo("lorem_");
        assertThat(NameTransformer.parse("lorem_{}").transform("dolor")).isEqualTo("lorem_dolor");
        assertThat(NameTransformer.parse("{}_ipsum").transform("dolor")).isEqualTo("dolor_ipsum");
        assertThat(NameTransformer.parse("lorem_{}_ipsum").transform("dolor")).isEqualTo("lorem_dolor_ipsum");
        assertThat(NameTransformer.parse("lorem_{}_ipsum_{}{}_sit").transform("dolor")).isEqualTo("lorem_dolor_ipsum_dolordolor_sit");
    }

    @Test
    void testLower() {
        assertThat(NameTransformer.parse("{lower}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{lower}").transform("DoLoR")).isEqualTo("dolor");
        assertThat(NameTransformer.parse("Lorem_{lower}_Ipsum").transform("DoLoR")).isEqualTo("Lorem_dolor_Ipsum");
        assertThat(NameTransformer.parse("Lorem_{lower}_Ipsum_{}").transform("DoLoR")).isEqualTo("Lorem_dolor_Ipsum_DoLoR");
    }

    @Test
    void testUpper() {
        assertThat(NameTransformer.parse("{upper}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{upper}").transform("DoLoR")).isEqualTo("DOLOR");
        assertThat(NameTransformer.parse("Lorem_{upper}_Ipsum").transform("DoLoR")).isEqualTo("Lorem_DOLOR_Ipsum");
        assertThat(NameTransformer.parse("Lorem_{upper}_Ipsum_{}").transform("DoLoR")).isEqualTo("Lorem_DOLOR_Ipsum_DoLoR");
    }

    @Test
    void testMax() {
        assertThat(NameTransformer.parse("{3}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{3}").transform("DoLoR")).isEqualTo("DoL");
        assertThat(NameTransformer.parse("{20}").transform("DoLoR")).isEqualTo("DoLoR");
        assertThat(NameTransformer.parse("{12}").transform("loremipsumdolorsitamet")).isEqualTo("loremipsumdo");
        assertThat(NameTransformer.parse("Lorem_{3}_Ipsum").transform("DoLoR")).isEqualTo("Lorem_DoL_Ipsum");
        assertThat(NameTransformer.parse("Lorem_{3}_Ipsum_{}").transform("DoLoR")).isEqualTo("Lorem_DoL_Ipsum_DoLoR");
        assertThat(NameTransformer.parse("Lorem_{3}_Ipsum_{1}_{23}").transform("DoLoR")).isEqualTo("Lorem_DoL_Ipsum_D_DoLoR");
    }

    @Test
    void testChain() {
        assertThat(NameTransformer.parse("{lower|3}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{4|2}").transform("DoLoR")).isEqualTo("Do");
        assertThat(NameTransformer.parse("{2|4}").transform("DoLoR")).isEqualTo("Do");
        assertThat(NameTransformer.parse("{lower|3}").transform("DoLoR")).isEqualTo("dol");
        assertThat(NameTransformer.parse("{upper|4}").transform("DoLoR")).isEqualTo("DOLO");
        assertThat(NameTransformer.parse("{upper|lower}").transform("DoLoR")).isEqualTo("dolor");
    }

    @Test
    void testComplex() {
        assertThat(NameTransformer.parse("\\|{}\\{}").transform("dolor")).isEqualTo("|dolor{}");
        assertThat(NameTransformer.parse("lorem{upper}_\\{}_{3|lower|2}_{}\\\\\\sit").transform("DoLoR"))
                .isEqualTo("loremDOLOR_{}_do_DoLoR\\sit");
    }

}
