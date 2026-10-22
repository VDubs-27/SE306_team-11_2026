package lavs.ui.customer.wizard;

import javafx.fxml.FXML;

import lavs.ui.customer.forms.CustomerDetailsFormController;

/**
 * Step 1 of the new customer wizard: the customer details form in a card. The form itself is
 * shared with Edit on the customer's profile.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class DetailsStepController {

    @FXML private CustomerDetailsFormController formController;

    /** True once anything has been entered, so cancelling should ask first. */
    boolean hasInput() {
        return formController.hasInput();
    }

    /** Checks the details, showing any problems; when they pass, saves them in the draft. */
    boolean check(NewCustomerDraft draft) {
        if (!formController.check()) {
            return false;
        }
        draft.setDetails(formController.read());
        return true;
    }
}
