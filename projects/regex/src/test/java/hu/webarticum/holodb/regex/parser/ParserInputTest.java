package hu.webarticum.holodb.regex.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class ParserInputTest {

    @Test
    void testConstructandContent() {
        assertThat(new ParserInput("loremipsum").content()).isEqualTo("loremipsum");
    }

    @Test
    void testEmptyState() {
        ParserInput parserInput = new ParserInput("");
        assertAtStart(parserInput);
        assertNoMoreInput(parserInput);
    }

    @Test
    void testNonEmptyStates() {
        ParserInput parserInput = new ParserInput("abcd");
        assertAtStart(parserInput);
        assertInMiddleState(parserInput, 0, 'a');
        assertNext(parserInput, 'a');
        assertInMiddleState(parserInput, 1, 'b');
        assertNext(parserInput, 'b');
        assertInMiddleState(parserInput, 2, 'c');
        assertNext(parserInput, 'c');
        assertInMiddleState(parserInput, 3, 'd');
        assertNext(parserInput, 'd');
        assertNoMoreInput(parserInput);
        parserInput.storno();
        parserInput.storno();
        assertInMiddleState(parserInput, 2, 'c');
        parserInput.storno();
        parserInput.storno();
        assertAtStart(parserInput);
        assertInMiddleState(parserInput, 0, 'a');
        assertExpect(parserInput, 'a');
        assertInMiddleState(parserInput, 1, 'b');
        assertExpect(parserInput, 'b');
        assertInMiddleState(parserInput, 2, 'c');
        assertExpect(parserInput, 'c');
        assertInMiddleState(parserInput, 3, 'd');
        assertExpect(parserInput, 'd');
        assertNoMoreInput(parserInput);
    }

    private void assertAtStart(ParserInput parserInput) {
        assertThatThrownBy(parserInput::storno).isInstanceOf(NoSuchElementException.class);
    }

    private void assertInMiddleState(
            ParserInput parserInput, int expectedPosition, char expectedPeekChar) {
        assertThat(parserInput.position()).isEqualTo(expectedPosition);
        assertThat(parserInput.hasNext()).isTrue();
        assertThat(parserInput.peek()).isEqualTo(expectedPeekChar);
    }

    private void assertNext(ParserInput parserInput, char expectedNextResult) {
        char c = parserInput.next();
        assertThat(c).isEqualTo(expectedNextResult);
    }

    private void assertExpect(ParserInput parserInput, char expectedNextResult) {
        assertThat(parserInput.expect(expectedNextResult)).isTrue();
    }

    private void assertNoMoreInput(ParserInput parserInput) {
        assertThat(parserInput.expect('a')).isFalse();
        assertThatThrownBy(parserInput::peek).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(parserInput::next).isInstanceOf(NoSuchElementException.class);
    }

}
