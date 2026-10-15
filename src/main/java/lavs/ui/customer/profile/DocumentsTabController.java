package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
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

    private RecordEditor<DocumentFile> editor;

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(documentList, "document", documentForm, documentFormController);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<DocumentRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(documentList, items.stream()
                .map(document -> DocumentRows.tile(document.type(), document.fileName(), document.fileType(),
                        document.size(), () -> handleEditDocument(document)))
                .toList(), "No documents recorded."));
    }

    @FXML
    private void handleAddDocument() {
        editor.add(false, document -> {
            // Later: CustomerService.addDocument(customerNumber, document), then reload the tab
        });
    }

    private void handleEditDocument(DocumentRow document) {
        DocumentFile file = new DocumentFile(document.type(), document.fileName(), document.fileType(),
                document.size());
        editor.edit(file, changed -> {
            // Later: CustomerService.updateDocument(document.id(), changed), then reload the tab
        }, () -> {
            // Later: CustomerService.removeDocument(document.id()), then reload the tab
        });
    }
}
