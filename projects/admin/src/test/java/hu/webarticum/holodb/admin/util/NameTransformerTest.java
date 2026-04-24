package hu.webarticum.holodb.admin.util;

import static org.assertj.core.api.Assertions.assertThat;

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
    void testConst() {
        assertThat(NameTransformer.parse("{const}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{const}").transform("dolor")).isEqualTo("DOLOR");
        assertThat(NameTransformer.parse("{const}").transform("loremIPSUMDolorSit123AMET"))
                .isEqualTo("LOREM_IPSUM_DOLOR_SIT_123_AMET");
        assertThat(NameTransformer.parse("{const}").transform("dolor_SIT")).isEqualTo("DOLOR_SIT");
        assertThat(NameTransformer.parse("{const}").transform("LoReM1__IPsum:d0l0r.")).isEqualTo("LOREM1_IPSUM_D0L0R");
        assertThat(NameTransformer.parse("lorem_{const}_ipsum_{}").transform("DOLor")).isEqualTo("lorem_DO_LOR_ipsum_DOLor");
    }

    @Test
    void testSnake() {
        assertThat(NameTransformer.parse("{snake}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{snake}").transform("dolor")).isEqualTo("dolor");
        assertThat(NameTransformer.parse("{snake}").transform("loremIPSUMDolorSit123AMET"))
                .isEqualTo("lorem_ipsum_dolor_sit_123_amet");
        assertThat(NameTransformer.parse("{snake}").transform("dolor_SIT")).isEqualTo("dolor_sit");
        assertThat(NameTransformer.parse("{snake}").transform("LoReM1__IPsum:d0l0r.")).isEqualTo("lorem1_ipsum_d0l0r");
        assertThat(NameTransformer.parse("lorem_{snake}_ipsum_{}").transform("DOLor")).isEqualTo("lorem_do_lor_ipsum_DOLor");
    }

    @Test
    void testCamel() {
        assertThat(NameTransformer.parse("{camel}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{camel}").transform("dolor")).isEqualTo("dolor");
        assertThat(NameTransformer.parse("{camel}").transform("loremIPSUMDolorSit123AMET"))
                .isEqualTo("loremIpsumDolorSit123Amet");
        assertThat(NameTransformer.parse("{camel}").transform("dolor_SIT")).isEqualTo("dolorSit");
        assertThat(NameTransformer.parse("{camel}").transform("LoReM1__IPsum:d0l0r.")).isEqualTo("lorem1IpsumD0l0r");
        assertThat(NameTransformer.parse("lorem_{camel}_ipsum_{}").transform("DOLor")).isEqualTo("lorem_doLor_ipsum_DOLor");
    }

    @Test
    void testPascal() {
        assertThat(NameTransformer.parse("{pascal}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{pascal}").transform("dolor")).isEqualTo("Dolor");
        assertThat(NameTransformer.parse("{pascal}").transform("loremIPSUMDolorSit123AMET"))
                .isEqualTo("LoremIpsumDolorSit123Amet");
        assertThat(NameTransformer.parse("{pascal}").transform("dolor_SIT")).isEqualTo("DolorSit");
        assertThat(NameTransformer.parse("{pascal}").transform("LoReM1__IPsum:d0l0r.")).isEqualTo("Lorem1IpsumD0l0r");
        assertThat(NameTransformer.parse("lorem_{pascal}_ipsum_{}").transform("DOLor")).isEqualTo("lorem_DoLor_ipsum_DOLor");
    }

    @Test
    void testAscii() {
        assertThat(NameTransformer.parse("{ascii}").transform("")).isEqualTo("");
        assertThat(NameTransformer.parse("{ascii}").transform("lorem")).isEqualTo("lorem");
        assertThat(NameTransformer.parse("{ascii}").transform("háztűznéző")).isEqualTo("haztuznezo");
        assertThat(NameTransformer.parse("{ascii}").transform("űrlényﬁ$𝕳𝖊𝖑𝖑𝖔▒𝟙𝟚𝟛")).isEqualTo("urlenyfi$Hello123");
        assertThat(NameTransformer.parse("lorem_{ascii}_ipsum_{}").transform("űr")).isEqualTo("lorem_ur_ipsum_űr");
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
