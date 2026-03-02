package hu.webarticum.holodb.benchmark.matcher;

import static org.assertj.core.api.Assertions.assertThatCode;

import hu.webarticum.miniconnect.api.MiniColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredColumnHeader;
import hu.webarticum.miniconnect.impl.result.StoredValueDefinition;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.type.StandardValueType;

import org.junit.jupiter.api.Test;

class DefaultTableHeaderMatcherTest {

    @Test
    void testMatch() {
        ImmutableList<MiniColumnHeader> columnHeaders = ImmutableList.of(
                buildColumnHeader("id", false, StandardValueType.INT),
                buildColumnHeader("label", false, StandardValueType.STRING));
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.empty()).match(columnHeaders))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(h -> false)).match(columnHeaders))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true)).match(columnHeaders))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true, h -> false)).match(columnHeaders))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true, h -> true)).match(columnHeaders))
                .doesNotThrowAnyException();
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(h -> true, h -> true, h -> true)).match(columnHeaders))
                .isInstanceOf(MatchFailedException.class);
        assertThatCode(() -> DefaultTableHeaderMatcher.of(ImmutableList.of(
                h -> h.name().equals("id"), h -> h.name().startsWith("la"))).match(columnHeaders))
                .doesNotThrowAnyException();
    }

    private MiniColumnHeader buildColumnHeader(String name, boolean isNullable, StandardValueType type) {
        return StoredColumnHeader.of(name, isNullable, StoredValueDefinition.of(type.name()));
    }

}
