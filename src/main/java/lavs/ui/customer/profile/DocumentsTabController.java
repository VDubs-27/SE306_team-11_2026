package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Documents tab: the files recorded for the customer, such as a payslip or licence, a page at a time.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<DocumentRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(documentList, items.stream()
                .map(document -> DocumentRows.tile(document.type(), document.fileName(), document.fileType(),
                        document.size(), () -> handleEditDocument(document)))
                .toList(), "No documents recorded."));
    }

    @FXML
    private void handleAddDocument() {
        // Later: open the add document form
    }

    private void handleEditDocument(DocumentRow document) {
        // Later: open the document details for this document
    }
}
