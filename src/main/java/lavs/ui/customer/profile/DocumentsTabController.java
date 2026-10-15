package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Documents tab: the files recorded for the customer, such as a payslip or licence, a page at a time.
 * Add and Edit use the same document pop-up as the new loan wizard and the loan screen.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    @FXML private VBox documentList;
    @FXML private PagerController pagerController;
    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<DocumentRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(documentList, items.stream()
                .map(document -> DocumentRows.tile(document.type(), document.fileName(), document.fileType(),
                        document.size(), () -> handleEditDocument(document)))
                .toList(), "No documents recorded."));
    }

    @FXML
    private void handleAddDocument() {
        documentFormController.clear();
        if (FormDialog.show(documentList, "Add document", documentForm, documentFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: CustomerService.addDocument(customerNumber, documentFormController.read()), then reload the tab
        }
    }

    private void handleEditDocument(DocumentRow document) {
        documentFormController.fill(new DocumentFile(document.type(), document.fileName(), document.fileType(),
                document.size()));
        switch (FormDialog.show(documentList, "Edit document", documentForm, documentFormController::check, true)) {
            case SAVED -> {
                // Later: CustomerService.updateDocument(document.id(), documentFormController.read()), then reload the tab
            }
            case REMOVED -> {
                // Later: CustomerService.removeDocument(document.id()), then reload the tab
            }
            default -> {
                // Cancelled: nothing to do
            }
        }
    }
}
