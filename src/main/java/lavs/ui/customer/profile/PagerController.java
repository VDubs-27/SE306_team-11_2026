package lavs.ui.customer.profile;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import lavs.logic.model.Page;

/** Prev and Next buttons for a paged list in a profile tab. */
public class PagerController {

    @FXML private HBox pager;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private LongConsumer onPageRequested = page -> { };
    private long currentPage = 1;

    /** Called with the page number the user wants next. */
    void setOnPageRequested(LongConsumer onPageRequested) {
        this.onPageRequested = onPageRequested;
    }

    /** Updates the buttons and label for the page now showing; hides the pager when there's only one page. */
    void show(Page<?> page) {
        currentPage = page.page();
        pageLabel.setText("Page " + page.page() + " of " + page.totalPages());
        prevButton.setDisable(!page.hasPrevious());
        nextButton.setDisable(!page.hasNext());

        boolean needed = page.totalPages() > 1;
        pager.setVisible(needed);
        pager.setManaged(needed);
    }

    @FXML
    private void handlePrev() {
        changePage(currentPage - 1);
    }

    @FXML
    private void handleNext() {
        changePage(currentPage + 1);
    }

    /** Loads the page, then scrolls back to the top so the new items start in view. */
    private void changePage(long page) {
        onPageRequested.accept(page);
        for (Parent parent = pager.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof ScrollPane scrollPane) {
                scrollPane.setVvalue(0);
                return;
            }
        }
    }
}
