package lavs.logic.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PageTests {

    private static Page<String> page(long page, long size, long total) {
        return new Page<>(List.of(), page, size, total);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "1, 1",
        "24, 1",
        "25, 1",
        "26, 2",
        "50, 2",
        "51, 3"
    })
    void totalPagesRoundsUp(long total, long expectedPages) {
        Assertions.assertEquals(expectedPages, page(1, 25, total).totalPages());
    }

    @Test
    void emptyResultHasNoNextOrPrevious() {
        Page<String> empty = page(1, 25, 0);

        Assertions.assertAll(
                () -> Assertions.assertFalse(empty.hasNext()),
                () -> Assertions.assertFalse(empty.hasPrevious())
        );
    }

    @Test
    void firstOfSeveralPagesHasNextButNoPrevious() {
        Page<String> first = page(1, 25, 51);

        Assertions.assertAll(
                () -> Assertions.assertTrue(first.hasNext()),
                () -> Assertions.assertFalse(first.hasPrevious())
        );
    }

    @Test
    void middlePageHasNextAndPrevious() {
        Page<String> middle = page(2, 25, 51);

        Assertions.assertAll(
                () -> Assertions.assertTrue(middle.hasNext()),
                () -> Assertions.assertTrue(middle.hasPrevious())
        );
    }

    @Test
    void lastPageHasPreviousButNoNext() {
        Page<String> last = page(3, 25, 51);

        Assertions.assertAll(
                () -> Assertions.assertFalse(last.hasNext()),
                () -> Assertions.assertTrue(last.hasPrevious())
        );
    }

    @Test
    void exactlyFullLastPageHasNoNext() {
        Assertions.assertFalse(page(2, 25, 50).hasNext());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void pageBelowOneIsRejected(long pageNumber) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> page(pageNumber, 25, 0));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    void sizeBelowOneIsRejected(long size) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> page(1, size, 0));
    }

    @Test
    void negativeTotalIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> page(1, 25, -1));
    }

    @Test
    void smallestValidPageIsAccepted() {
        Assertions.assertEquals(0, page(1, 1, 0).totalPages());
    }

    @Test
    void missingItemsAreRejected() {
        Assertions.assertThrows(NullPointerException.class, () -> new Page<String>(null, 1, 25, 0));
    }

    @Test
    void itemsAreCopiedSoLaterChangesDoNotLeakIn() {
        List<String> source = new ArrayList<>(List.of("a"));
        Page<String> result = new Page<>(source, 1, 25, 1);

        source.add("b");

        Assertions.assertEquals(List.of("a"), result.items());
    }

    @Test
    void itemsCannotBeModified() {
        Page<String> result = new Page<>(List.of("a"), 1, 25, 1);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> result.items().add("b"));
    }
}
