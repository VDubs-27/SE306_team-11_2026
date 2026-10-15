package lavs.ui.loan.wizard;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.EditableList;
import lavs.ui.common.LoanText;
import lavs.ui.common.RecordEditor;
import lavs.ui.common.ReviewRows;

/**
 * Step 2 of the new loan wizard: supporting documents (optional), and a read-only review of the
 * loan details with a link back to change them.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class UploadStepController {

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private VBox summary;

    private NewLoanDraft draft;
    private EditableList<DocumentFile> documents;
    private Runnable onChangeDetails = () -> { };

    @FXML
    private void initialize() {
        documents = new EditableList<>(
                new RecordEditor<>(documentList, "document", documentForm, documentFormController),
                documentList, DocumentRows::tile, "No documents added yet.");
    }

    /** Gives the step the draft whose documents it edits. */
    void use(NewLoanDraft draft) {
        this.draft = draft;
        documents.use(draft.documents());
    }

    /** What the "Change" link does: the wizard goes back to Loan details. */
    void setOnChangeDetails(Runnable onChangeDetails) {
        this.onChangeDetails = onChangeDetails;
    }

    /** Fills the review from the loan details just entered; runs each time the step shows. */
    void showSummary() {
        Loan.Details details = draft.details();
        summary.getChildren().setAll(
                ReviewRows.summaryRow("Principal", DisplayFormat.money(details.principal())),
                ReviewRows.summaryRow("Interest", LoanText.interest(details)),
                ReviewRows.summaryRow("Repayments", LoanText.repayment(details)),
                ReviewRows.summaryRow("Start date", DisplayFormat.date(details.startDate())),
                ReviewRows.summaryRow("Renewal date", DisplayFormat.date(details.renewalDate())));
    }

    @FXML
    private void handleAddDocument() {
        documents.add();
    }

    @FXML
    private void handleChangeDetails() {
        onChangeDetails.run();
    }

}
