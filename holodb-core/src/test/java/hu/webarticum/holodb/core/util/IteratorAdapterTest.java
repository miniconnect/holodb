package hu.webarticum.holodb.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;

class IteratorAdapterTest {

    @Test
    void testSimpleMapping() {
        List<Integer> numbers = Arrays.asList(2, 4, 12, 5, 33, 4, -8, 0, 1);
        
        Iterator<String> mappedIterator = new IteratorAdapter<Integer, String>(
                numbers.iterator(), number -> String.format("(%d)", number));
        
        assertThat(mappedIterator).toIterable().containsExactly(
                "(2)", "(4)", "(12)", "(5)", "(33)", "(4)", "(-8)", "(0)", "(1)");
    }
    
}
