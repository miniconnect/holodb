package hu.webarticum.holodb.regex.facade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import hu.webarticum.miniconnect.lang.FindPositionResult;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.lang.LargeInteger;

class MatchListTest {

    @Test
    void testZeroLength() {
        MatchList matchList = MatchList.of("");
        assertThat(matchList.size()).isEqualTo(LargeInteger.ONE);
        assertThat(matchList.get(LargeInteger.ZERO)).isEmpty();
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(matchList.find("a")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(matchList.iterator().next()).isEmpty();
        assertThat(matchList.iterator(LargeInteger.ZERO).next()).isEmpty();
    }

    @Test
    void testStatic() {
        MatchList matchList = MatchList.of("lorem");
        assertThat(matchList.size()).isEqualTo(LargeInteger.ONE);
        assertThat(matchList.get(LargeInteger.ZERO)).isEqualTo("lorem");
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("a")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("lorem")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(matchList.find("loremx")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(matchList.find("zzz")).isEqualTo(FindPositionResult.notFound(LargeInteger.ONE));
        assertThat(matchList.iterator().next()).isEqualTo("lorem");
        assertThat(matchList.iterator(LargeInteger.ZERO).next()).isEqualTo("lorem");
    }

    @Test
    void testSimple() {
        MatchList matchList = MatchList.of("(lorem|ipsum)[azx]");
        assertThat(matchList.size()).isEqualTo(LargeInteger.SIX);
        assertThat(ImmutableList.fill(6, matchList::get)).containsExactly(
                "ipsuma", "ipsumx", "ipsumz", "lorema", "loremx", "loremz");
        assertThat(ImmutableList.fill(6, i -> matchList.get(LargeInteger.of(i)))).containsExactly(
                "ipsuma", "ipsumx", "ipsumz", "lorema", "loremx", "loremz");
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("ipsum")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("lorem")).isEqualTo(FindPositionResult.notFound(LargeInteger.THREE));
        assertThat(matchList.find("loremz")).isEqualTo(FindPositionResult.found(LargeInteger.FIVE));
        assertThat(matchList.find("zzz")).isEqualTo(FindPositionResult.notFound(LargeInteger.SIX));
        assertThat(matchList).containsExactly(
                "ipsuma", "ipsumx", "ipsumz", "lorema", "loremx", "loremz");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.ZERO)).containsExactly(
                "ipsuma", "ipsumx", "ipsumz", "lorema", "loremx", "loremz");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.THREE)).containsExactly(
                "lorema", "loremx", "loremz");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.SIX)).isEmpty();
    }

    @Test
    void testComplex1() {
        MatchList matchList = MatchList.of("f{0,2}[ra](t[tu]|tue?)s?");
        assertThat(matchList.size()).isEqualTo(LargeInteger.of(36));
        String[] matches = new String[] {
                "att", "atts", "atu", "atue", "atues", "atus",
                "fatt", "fatts", "fatu", "fatue", "fatues", "fatus",
                "ffatt", "ffatts", "ffatu", "ffatue", "ffatues", "ffatus",
                "ffrtt", "ffrtts", "ffrtu", "ffrtue", "ffrtues", "ffrtus",
                "frtt", "frtts", "frtu", "frtue", "frtues", "frtus",
                "rtt", "rtts", "rtu", "rtue", "rtues", "rtus",
        };
        assertThat(ImmutableList.fill(36, matchList::get)).containsExactly(matches);
        assertThat(ImmutableList.fill(36, i -> matchList.get(LargeInteger.of(i)))).containsExactly(matches);
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("at")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("att")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(matchList.find("atz")).isEqualTo(FindPositionResult.notFound(LargeInteger.SIX));
        assertThat(matchList.find("ffrtts")).isEqualTo(FindPositionResult.found(LargeInteger.of(19)));
        assertThat(matchList.find("krs")).isEqualTo(FindPositionResult.notFound(LargeInteger.of(30)));
        assertThat(matchList.find("rtus")).isEqualTo(FindPositionResult.found(LargeInteger.of(35)));
        assertThat(matchList.find("ywttu")).isEqualTo(FindPositionResult.notFound(LargeInteger.of(36)));
        assertThat(matchList).containsExactly(matches);
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.ZERO)).containsExactly(matches);
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(22))).containsExactly(
                "ffrtues", "ffrtus", "frtt", "frtts", "frtu", "frtue", "frtues", "frtus",
                "rtt", "rtts", "rtu", "rtue", "rtues", "rtus");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(31))).containsExactly(
                "rtts", "rtu", "rtue", "rtues", "rtus");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(36))).isEmpty();
    }

    @Test
    void testComplex2() {
        MatchList matchList = MatchList.of("[t-vg]?(lo[pr]em|[el]or[ea][mn])");
        assertThat(matchList.size()).isEqualTo(LargeInteger.of(45));
        String[] matches = new String[] {
                "eoram", "eoran", "eorem", "eoren", "georam", "georan", "georem", "georen",
                "glopem", "gloram", "gloran", "glorem", "gloren",
                "lopem", "loram", "loran", "lorem", "loren", "teoram", "teoran", "teorem", "teoren",
                "tlopem", "tloram", "tloran", "tlorem", "tloren",
                "ueoram", "ueoran", "ueorem", "ueoren", "ulopem", "uloram", "uloran", "ulorem", "uloren",
                "veoram", "veoran", "veorem", "veoren", "vlopem", "vloram", "vloran", "vlorem", "vloren",
        };
        assertThat(ImmutableList.fill(45, matchList::get)).containsExactly(matches);
        assertThat(ImmutableList.fill(45, i -> matchList.get(LargeInteger.of(i)))).containsExactly(matches);
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("bwer")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("eora")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("eoram")).isEqualTo(FindPositionResult.found(LargeInteger.ZERO));
        assertThat(matchList.find("nztu")).isEqualTo(FindPositionResult.notFound(LargeInteger.of(18)));
        assertThat(matchList.find("vlorenz")).isEqualTo(FindPositionResult.notFound(LargeInteger.of(45)));
        assertThat(matchList).containsExactly(matches);
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.ZERO)).containsExactly(matches);
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(27))).containsExactly(
                "ueoram", "ueoran", "ueorem", "ueoren", "ulopem", "uloram", "uloran", "ulorem", "uloren",
                "veoram", "veoran", "veorem", "veoren", "vlopem", "vloram", "vloran", "vlorem", "vloren");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(42))).containsExactly(
                "vloran", "vlorem", "vloren");
        assertThat((Iterable<String>) () -> matchList.iterator(LargeInteger.of(45))).isEmpty();
    }

    @Test
    void testHuge() {
        MatchList matchList = MatchList.builder().groupRepeatLimit(12).build("\\d*[xyz]?(lorem|lo[rp][ea][mn]u)u?\\d*");
        LargeInteger expectedSize = LargeInteger.of("83950617283933827160493828");
        assertThat(matchList.size()).isEqualTo(expectedSize);
        String[] firstSevenMatches = new String[] {
                "000000000000lopamu", "000000000000lopamu0", "000000000000lopamu00", "000000000000lopamu000",
                "000000000000lopamu0000", "000000000000lopamu00000", "000000000000lopamu000000",
        };
        assertThat(matchList.get(LargeInteger.of("10364273645091789362621779")))
                .isEqualTo("123456789012ylopemuu543210987654");
        assertThat(matchList.find("")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("00000")).isEqualTo(FindPositionResult.notFound(LargeInteger.ZERO));
        assertThat(matchList.find("123456789012ylopemuu543210987654")).isEqualTo(
                FindPositionResult.found(LargeInteger.of("10364273645091789362621779")));
        assertThat(matchList.find("zlorenuu999999999999")).isEqualTo(
                FindPositionResult.found(LargeInteger.of("83950617283933827160493827")));
        assertThat(matchList.find("zrthsk")).isEqualTo(
                FindPositionResult.notFound(LargeInteger.of("83950617283933827160493828")));
        assertThat(matchList.iterator(LargeInteger.of("10364273645091789362621779")).next())
                .isEqualTo("123456789012ylopemuu543210987654");
        assertThat(ImmutableList.fill(7, matchList::get)).containsExactly(firstSevenMatches);
        assertThat(ImmutableList.fill(7, i -> matchList.get(LargeInteger.of(i)))).containsExactly(firstSevenMatches);
        assertThat((Iterable<String>) () -> matchList.iterator(expectedSize.subtract(LargeInteger.THREE)))
                .containsExactly("zlorenuu999999999997", "zlorenuu999999999998", "zlorenuu999999999999");
        assertThat((Iterable<String>) () -> matchList.iterator(expectedSize)).isEmpty();
    }

    @Test
    void testRandom() {
        MatchList matchList = MatchList.builder().seed(42).build("f{0,2}[ra](t[tu]|tue?)s?");
        assertThat(matchList.size()).isEqualTo(LargeInteger.of(36));
        ImmutableList<String> matches = ImmutableList.of(
                "att", "atts", "atu", "atue", "atues", "atus",
                "fatt", "fatts", "fatu", "fatue", "fatues", "fatus",
                "ffatt", "ffatts", "ffatu", "ffatue", "ffatues", "ffatus",
                "ffrtt", "ffrtts", "ffrtu", "ffrtue", "ffrtues", "ffrtus",
                "frtt", "frtts", "frtu", "frtue", "frtues", "frtus",
                "rtt", "rtts", "rtu", "rtue", "rtues", "rtus"
        );
        assertThat(ImmutableList.fill(1000, i -> matchList.random())).isSubsetOf(matches);
    }

}
