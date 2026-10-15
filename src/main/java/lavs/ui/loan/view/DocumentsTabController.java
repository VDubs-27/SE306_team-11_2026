package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

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

    private final List<DocumentFile> documents = new ArrayList<>();
    private Runnable onChanged = () -> { };

    /** What happens after a document is added, changed or removed: the loan screen redraws this tab and its count. */
    void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    /** The loan's documents when the screen opens. */
    void use(List<DocumentFile> documents) {
        this.documents.clear();
        this.documents.addAll(documents);
    }

    /** Shows the documents and returns how many there are, for the tab's count. */
    long show() {
        return pagerController.show(page -> SampleLoans.pageOf(List.copyOf(documents), page, PAGE_SIZE), this::draw);
    }

    @FXML
    private void handleAddDocument() {
        documentFormController.clear();
        if (FormDialog.show(documentList, "Add document", documentForm, documentFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: LoanService.addDocument(loan, ...); until then the change only shows on this screen
            documents.add(documentFormController.read());
            onChanged.run();
        }
    }

    private void handleEditDocument(DocumentFile document) {
        documentFormController.fill(document);
        FormDialog.Result result = FormDialog.show(documentList, "Edit document", documentForm,
                documentFormController::check, true);
        // Later: LoanService.updateDocument / deleteDocument; until then the change only shows on this screen
        if (result == FormDialog.Result.SAVED) {
            documents.set(documents.indexOf(document), documentFormController.read());
            onChanged.run();
        } else if (result == FormDialog.Result.REMOVED) {
            documents.remove(document);
            onChanged.run();
        }
    }

    private void draw(List<DocumentFile> page) {
        RecordTiles.fill(documentList, page.stream()
                .map(document -> DocumentRows.tile(document.type(), document.fileName(), document.fileType(),
                        document.size(), () -> handleEditDocument(document)))
                .toList(), "No documents recorded.");
    }
}
