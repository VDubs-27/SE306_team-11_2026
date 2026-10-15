package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.EditableList;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;

/**
 * The loan screen's Documents tab: the files recorded for the loan, a page at a time. Documents
 * can be added, changed or removed whatever the loan's status, since paperwork can arrive late.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    private EditableList<DocumentFile> documents;

    @FXML
    private void initialize() {
        // Later: save through LoanService (add, update and delete document); until then changes only show on this screen
        documents = new EditableList<>(
                new RecordEditor<>(documentList, "document", documentForm, documentFormController),
                documentList, DocumentRows::tile, "No documents recorded.")
                .paged(pagerController, PAGE_SIZE);
    }

    /** What happens after a document is added, changed or removed: the loan screen updates the tab's count. */
    void setOnChanged(Runnable onChanged) {
        documents.onChanged(onChanged);
    }

    /** Shows the loan's documents from the first page. */
    void use(List<DocumentFile> loanDocuments) {
        documents.use(new ArrayList<>(loanDocuments));
    }

    /** How many documents there are, for the tab's count. */
    long count() {
        return documents.items().size();
    }

    @FXML
    private void handleAddDocument() {
        documents.add();
    }
}
