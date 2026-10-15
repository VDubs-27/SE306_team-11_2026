package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The loan screen's Documents tab: the files recorded for the loan, a page at a time. While the
 * loan is New, documents can be added, changed or removed; after that they're locked.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private PagerController pagerController;
    @FXML private Button addButton;
    @FXML private Label lockNote;

    private final List<DocumentFile> documents = new ArrayList<>();
    private Loan loan;
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

    /** Shows the documents, with Add and Edit only while the loan is New. Returns how many there are. */
    long show(Loan loan) {
        this.loan = loan;
        boolean editable = isEditable();
        addButton.setVisible(editable);
        addButton.setManaged(editable);
        lockNote.setText(editable
                ? "LAVS records each file's name, type and size. The file itself stays with the customer's paperwork."
                : "This loan is " + loan.status().code().toLowerCase(Locale.ROOT)
                        + ", so its documents can no longer be changed.");
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

    /** One page of documents; Edit is offered only while the loan is New. */
    private void draw(List<DocumentFile> page) {
        boolean editable = isEditable();
        RecordTiles.fill(documentList, page.stream()
                .map(document -> DocumentRows.tile(document.type(), document.fileName(), document.fileType(),
                        document.size(), editable ? () -> handleEditDocument(document) : null))
                .toList(), "No documents recorded.");
    }

    private boolean isEditable() {
        return loan.status() == Loan.Status.NEW;
    }
}
