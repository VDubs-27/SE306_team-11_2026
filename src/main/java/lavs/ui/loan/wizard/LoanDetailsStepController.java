package lavs.ui.loan.wizard;

import javafx.fxml.FXML;

import lavs.ui.loan.forms.LoanDetailsFormController;

/**
 * Step 1 of the new loan wizard: the loan details form in a card. The form itself is shared with
 * Edit on the loan screen.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanDetailsStepController {

    @FXML private LoanDetailsFormController formController;

    /** True once anything has been entered, so cancelling should ask first. */
    boolean hasInput() {
        return formController.hasInput();
    }

    /** Checks the details, showing any problems; when they pass, saves them in the draft. */
    boolean check(NewLoanDraft draft) {
        if (!formController.check()) {
            return false;
        }
        draft.setDetails(formController.read());
        return true;
    }
}
