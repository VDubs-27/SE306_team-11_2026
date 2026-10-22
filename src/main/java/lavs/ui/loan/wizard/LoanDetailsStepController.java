package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;

import lavs.logic.model.FieldError;
import lavs.logic.service.Result;
import lavs.ui.common.FieldErrors;
import lavs.ui.loan.forms.LoanDetailsFormController;

/**
 * Step 1 of the new loan wizard: the loan details form in a card. The form itself is shared with
 * Edit on the loan screen.
 * AI-assisted: written by Jack, refined with Claude.
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

    /**
     * Shows why the mainframe turned the details down: problems with a field under that field.
     * Returns the rest, e.g. no connection, to show another way, or null when there are none.
     */
    String showProblems(Result<?> failed) {
        FieldErrors fieldErrors = formController.fieldErrors();
        fieldErrors.show(failed.fieldErrors());
        List<String> others = new ArrayList<>(
                fieldErrors.unplaced(failed.fieldErrors()).stream().map(FieldError::message).toList());
        if (failed.errorMessage() != null) {
            others.add(failed.errorMessage());
        }
        return others.isEmpty() ? null : String.join(" ", others);
    }
}
