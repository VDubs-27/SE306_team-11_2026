package lavs.ui.common;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import lavs.logic.model.Page;

/**
 * Pages a list on screen, such as a profile tab: loads a page, has the tab draw its items, and keeps the
 * Prev and Next buttons and "Page x of y" in step.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class PagerController {

    @FXML private HBox pager;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private LongConsumer showPage = number -> { };
    private long currentPage = 1;
    private long pageSize = 1;
    private long total;

    /**
     * Shows page 1, then any page the user moves to. loadPage fetches a page by number and draw
     * puts its items on screen. Returns how many items there are altogether, for the tab's count.
     */
    public <T> long show(LongFunction<Page<T>> loadPage, Consumer<List<T>> draw) {
        showPage = number -> {
            Page<T> page = loadPage.apply(number);
            draw.accept(page.items());
            update(page);
        };
        showPage.accept(1);
        return total;
    }

    /**
     * Shows the page that's open again, after the list changed. If the change emptied it, such as
     * removing the only item on the last page, shows the page before instead.
     */
    public void reload() {
        showPage.accept(currentPage);
        if (currentPage > 1 && total <= (currentPage - 1) * pageSize) {
            showPage.accept(currentPage - 1);
        }
        moveTo(false);
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
        pager.setVisible(needed);
        pager.setManaged(needed);
    }

    /** Loads the page, then resizes and scrolls to suit it. */
    private void changePage(long page) {
        showPage.accept(page);
        moveTo(true);
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
}
