package lavs.ui.loan.wizard;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentFile;
import lavs.ui.common.DocumentFormController;
import lavs.ui.common.DocumentRows;
import lavs.ui.common.FormDialog;
import lavs.ui.common.RecordTiles;

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
    private Runnable onChangeDetails = () -> { };

    /** Gives the step the draft whose documents it edits. */
    void use(NewLoanDraft draft) {
        this.draft = draft;
        refresh();
    }

    /** What the "Change" link does: the wizard goes back to Loan details. */
    void setOnChangeDetails(Runnable onChangeDetails) {
        this.onChangeDetails = onChangeDetails;
    }

    /** Fills the review from the loan details just entered; runs each time the step shows. */
    void showSummary() {
        Loan.Details details = draft.details();
        summary.getChildren().setAll(
                row("Principal", DisplayFormat.money(details.principal())),
                row("Interest", details.rateAmount().stripTrailingZeros().toPlainString() + "% "
                        + details.rateFrequency().code().toLowerCase(Locale.ROOT) + " · "
                        + LoanDetailsStepController.rateTypeName(details.rateType())),
                row("Repayments", DisplayFormat.moneyWithCents(details.paymentAmount()) + " "
                        + frequencyName(details.paymentFrequency())),
                row("Start date", DisplayFormat.date(details.startDate())),
                row("Renewal date", DisplayFormat.date(details.renewalDate())));
    }

    @FXML
    private void handleAddDocument() {
        documentFormController.clear();
        if (FormDialog.show(documentList, "Add document", documentForm, documentFormController::check, false)
                == FormDialog.Result.SAVED) {
            draft.documents().add(documentFormController.read());
        }
        refresh();
    }

    private void handleEditDocument(int index) {
        List<DocumentFile> documents = draft.documents();
        documentFormController.fill(documents.get(index));
        FormDialog.Result result = FormDialog.show(documentList, "Edit document", documentForm,
                documentFormController::check, true);
        if (result == FormDialog.Result.SAVED) {
            documents.set(index, documentFormController.read());
        } else if (result == FormDialog.Result.REMOVED) {
            documents.remove(index);
        }
        refresh();
    }

    @FXML
    private void handleChangeDetails() {
        onChangeDetails.run();
    }

    private void refresh() {
        List<DocumentFile> documents = draft.documents();
        RecordTiles.fill(documentList, IntStream.range(0, documents.size())
                .mapToObj(i -> DocumentRows.tile(documents.get(i).type(), documents.get(i).fileName(),
                        documents.get(i).fileType(), documents.get(i).size(), () -> handleEditDocument(i)))
                .toList(), "No documents added yet.");
    }

    /** e.g. "monthly", or "on renewal". */
    private static String frequencyName(Loan.PaymentFrequency frequency) {
        return frequency == Loan.PaymentFrequency.ON_RENEWAL ? "on renewal" : frequency.code().toLowerCase(Locale.ROOT);
    }

    /** "Principal ........ $450,000", with a line under it. */
    private static HBox row(String name, String value) {
        Label nameText = new Label(name);
        nameText.getStyleClass().add("text-muted");
        Label valueText = new Label(value);
        valueText.getStyleClass().add("review-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(nameText, spacer, valueText);
        row.getStyleClass().add("review-row");
        return row;
    }
}
