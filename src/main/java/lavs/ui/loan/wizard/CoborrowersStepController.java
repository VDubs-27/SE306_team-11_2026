package lavs.ui.loan.wizard;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.customer.wizard.NewCustomer;
import lavs.ui.loan.people.LoanPeopleController;
import lavs.ui.shell.CustomerServices;

/**
 * Step 3 of the new loan wizard: who else is on the loan. The applicant is listed first; others
 * are found among existing customers or created through the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CoborrowersStepController {

    @FXML private LoanPeopleController peopleController;
    @FXML private Label loanSummary;

    private NewLoanDraft draft;

    /** Gives the step the draft whose people it edits, and where to find customers to add. */
    void use(NewLoanDraft draft, CustomerServices services) {
        this.draft = draft;
        peopleController.useServices(services, LoanPeopleController.ON_SCREEN);
        peopleController.use(draft.applicant(), draft.others());
    }

    /** The loan this is for, in the heading: "$450,000 · Fixed · not yet submitted". */
    void showLoanSummary() {
        Loan.Details details = draft.details();
        // Later: show the loan number and status once submitting creates the loan
        loanSummary.setText(details == null
                ? "New loan · not yet submitted"
                : DisplayFormat.money(details.principal()) + " · "
                        + LoanText.rateTypeName(details.rateType())
                        + " · not yet submitted");
    }

    /** What "Add new customer" does: the wizard opens the new customer wizard. */
    void setOnAddNewCustomer(Runnable onAddNewCustomer) {
        peopleController.setOnAddNewCustomer(onAddNewCustomer);
    }

    /** Adds a customer just made in the new customer wizard to the loan. */
    void addNewCustomer(NewCustomer created) {
        peopleController.addNewCustomer(created.customer(), created.suburb(), created.employed());
    }

    /** An applicant who isn't employed needs a guarantor; otherwise anyone may move on. */
    boolean check() {
        return peopleController.check();
    }
}
