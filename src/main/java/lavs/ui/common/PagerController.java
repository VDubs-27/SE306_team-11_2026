package lavs.ui.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import lavs.logic.model.Page;
import lavs.logic.service.Result;

/**
 * Pages a list on screen, such as a profile tab: loads a page, has the tab draw its items, and keeps the
 * Prev and Next buttons and "Page x of y" in step. Pages already on hand show straight away (show);
 * pages read from the mainframe load in the background (showLoading).
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class PagerController {

    @FXML private HBox pager;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    /** Shows a page by number, then runs the second argument once it's on screen. */
    private PageShower showPage = (number, then) -> { };
    /** Counts pages asked for, so only the answer to the latest request is shown. */
    private long requests;
    private long currentPage = 1;
    private long pageSize = 1;
    private long total;

    /**
     * Shows page 1, then any page the user moves to. loadPage fetches a page by number and draw
     * puts its items on screen. Returns how many items there are altogether, for the tab's count.
     */
    public <T> long show(LongFunction<Page<T>> loadPage, Consumer<List<T>> draw) {
        requests++;
        showPage = (number, then) -> {
            Page<T> page = loadPage.apply(number);
            draw.accept(page.items());
            update(page);
            then.run();
        };
        showPage.show(1, () -> { });
        return total;
    }

    /**
     * Shows page 1, then any page the user moves to, reading each from the mainframe in the background
     * with loadPage. While a page loads, Prev and Next wait. Once it's read, draw puts its items on
     * screen, and onLoaded hears every answer: the page, for a count, or why it couldn't be read, in
     * which case what's showing stays. Starting again before an answer comes ignores that answer.
     */
    public <T> void showLoading(LongFunction<Result<Page<T>>> loadPage, Consumer<List<T>> draw,
                                Consumer<Result<Page<T>>> onLoaded) {
        showPage = (number, then) -> {
            long request = ++requests;
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            Background.run(() -> loadPage.apply(number), loaded -> {
                if (request != requests) {
                    return;
                }
                if (loaded.successful()) {
                    draw.accept(loaded.value().items());
                    update(loaded.value());
                    // The tab may already be showing, at its size before the items arrived
                    moveTo(false);
                    then.run();
                } else {
                    prevButton.setDisable(currentPage <= 1);
                    nextButton.setDisable(currentPage * pageSize >= total);
                }
                onLoaded.accept(loaded);
            });
        };
        showPage.show(1, () -> { });
    }

    /** Ignores any page still loading, e.g. when the list it was for is cleared. */
    public void cancel() {
        requests++;
    }

    /**
     * Shows the page that's open again, after the list changed. If the change emptied it, such as
     * removing the only item on the last page, shows the page before instead.
     */
    public void reload() {
        showPage.show(currentPage, () -> {
            if (currentPage > 1 && total <= (currentPage - 1) * pageSize) {
                showPage.show(currentPage - 1, () -> moveTo(false));
            } else {
                moveTo(false);
            }
        });
    }

    @FXML
    private void handlePrev() {
        changePage(currentPage - 1);
    }

    @FXML
    private void handleNext() {
        changePage(currentPage + 1);
    }

    /** Updates the buttons and label for the page now showing; hides the pager when there's only one page. */
    private void update(Page<?> page) {
        currentPage = page.page();
        pageSize = page.size();
        total = page.total();
        pageLabel.setText("Page " + page.page() + " of " + page.totalPages());
        prevButton.setDisable(!page.hasPrevious());
        nextButton.setDisable(!page.hasNext());

        boolean needed = page.totalPages() > 1;
        Nodes.show(pager, needed);
    }

    /** Loads the page, then resizes and scrolls to suit it. */
    private void changePage(long page) {
        showPage.show(page, () -> moveTo(true));
    }

    /**
     * A TabPane doesn't notice when the open tab's content changes size, so it is asked to lay out
     * again. After moving to another page, the screen also scrolls back to the top so the new items
     * start in view.
     */
    private void moveTo(boolean scrollToTop) {
        for (Parent parent = pager.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof TabPane tabPane) {
                tabPane.requestLayout();
            }
            if (parent instanceof ScrollPane scrollPane) {
                if (scrollToTop) {
                    scrollPane.setVvalue(0);
                }
                return;
            }
        }
    }

    /** Shows a page by number, then runs then once it's on screen. */
    @FunctionalInterface
    private interface PageShower {
        void show(long number, Runnable then);
    }
}
