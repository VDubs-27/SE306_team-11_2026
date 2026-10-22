package lavs.ui.common;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import lavs.logic.model.Page;
import lavs.logic.service.Result;
import lavs.ui.FxTesting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PagerControllerTests {

    private static final long PAGE_SIZE = 5;

    private final List<Long> requested = new ArrayList<>();
    private final List<List<Long>> drawn = new ArrayList<>();
    private long total;
    private FXMLLoader loader;

    @BeforeAll
    static void startJavaFx() {
        FxTesting.start();
    }

    @BeforeEach
    void loadPager() {
        loader = FxTesting.get(() -> {
            FXMLLoader pager = new FXMLLoader(PagerController.class.getResource("/lavs/ui/common/pager.fxml"));
            try {
                pager.load();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return pager;
        });
    }

    @Test
    void reloadReadsThePageThatWasOpenAgain() {
        showItems(12);
        next();

        FxTesting.run(() -> pager().reload());
        FxTesting.settle();

        assertAll(
                () -> assertEquals(List.of(1L, 2L, 2L), requested),
                () -> assertEquals(List.of(6L, 7L, 8L, 9L, 10L), drawn.get(drawn.size() - 1)),
                () -> assertEquals("Page 2 of 3", label())
        );
    }

    @Test
    void reloadGoesToThePageBeforeWhenTheOpenPageWasEmptied() {
        showItems(6);
        next();
        total = 5;

        FxTesting.run(() -> pager().reload());
        FxTesting.settle();

        assertAll(
                () -> assertEquals(List.of(1L, 2L, 2L, 1L), requested),
                () -> assertEquals(List.of(1L, 2L, 3L, 4L, 5L), drawn.get(drawn.size() - 1)),
                () -> assertEquals("Page 1 of 1", label())
        );
    }

    /** Shows a list of items numbered from 1, read a page at a time. */
    private void showItems(long count) {
        total = count;
        FxTesting.run(() -> pager().showLoading(this::read, drawn::add, loaded -> { }));
        FxTesting.settle();
    }

    private Result<Page<Long>> read(long page) {
        requested.add(page);
        long first = (page - 1) * PAGE_SIZE + 1;
        long last = Math.min(total, page * PAGE_SIZE);
        return Result.success(new Page<>(LongStream.rangeClosed(first, last).boxed().toList(), page, PAGE_SIZE, total));
    }

    private void next() {
        FxTesting.run(() -> ((Button) loader.getNamespace().get("nextButton")).fire());
        FxTesting.settle();
    }

    private PagerController pager() {
        return loader.getController();
    }

    private String label() {
        return FxTesting.get(() -> ((Label) loader.getNamespace().get("pageLabel")).getText());
    }
}
