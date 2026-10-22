package lavs.ui.loan.wizard;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.common.ReviewRows;

/**
 * Step 2 of the new loan wizard: a read-only review of the loan details with a link back to
 * change them.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class UploadStepController {

    @FXML private VBox summary;

    private NewLoanDraft draft;
    private Runnable onChangeDetails = () -> { };

    /** Gives the step the draft it reviews. */
    void use(NewLoanDraft draft) {
        this.draft = draft;
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
    private void handleChangeDetails() {
        onChangeDetails.run();
    }

}
