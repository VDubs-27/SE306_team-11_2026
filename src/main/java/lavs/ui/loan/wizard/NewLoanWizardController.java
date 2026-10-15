package lavs.ui.loan.wizard;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.Dialogs;
import lavs.ui.common.DisplayFormat;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;
import lavs.ui.wizard.WizardController;
import lavs.ui.wizard.WizardStep;

/**
 * The new loan wizard for one customer: loan details, upload and review, co-borrowers, then submit.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NewLoanWizardController implements NavigatorAware {

    @FXML private WizardController wizardController;
    @FXML private Node wizard;
    @FXML private Node loanDetails;
    @FXML private LoanDetailsStepController loanDetailsController;
    @FXML private Node upload;
    @FXML private UploadStepController uploadController;
    @FXML private Node coborrowers;
    @FXML private CoborrowersStepController coborrowersController;
    @FXML private Node submit;
    @FXML private SubmitStepController submitController;

    private Navigator navigator;
    private NewLoanDraft draft;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        wizardController.setTitle("New loan");
        wizardController.setFinishText("Submit loan");
        wizardController.setOnCancel(this::handleCancel);
        wizardController.setOnFinish(this::handleSubmit);
        wizardController.setSteps(List.of(
                new WizardStep("Loan details", loanDetails, () -> loanDetailsController.check(draft), null),
                new WizardStep("Upload & review", upload, null, () -> uploadController.showSummary()),
                new WizardStep("Co-borrowers", coborrowers, coborrowersController::check,
                        () -> coborrowersController.showLoanSummary()),
                new WizardStep("Submit", submit, null, () -> submitController.show(draft))));
    }

    /** Sets the wizard up for the customer the loan is for: their initials and "for John Smith · number". */
    public void start(Customer customer) {
        Label avatar = new Label(DisplayFormat.initials(customer));
        avatar.getStyleClass().add("avatar");
        wizardController.setIcon(avatar);
        wizardController.setSubtitle("for " + customer.fullName() + " · " + customer.number().value());
        draft = new NewLoanDraft(LoanPerson.lookUp(customer, null));
        uploadController.use(draft);
        uploadController.setOnChangeDetails(() -> wizardController.goBackTo(0));
        coborrowersController.use(draft);
        coborrowersController.setOnAddNewCustomer(
                () -> navigator.showNewCustomerWizard(coborrowersController::addNewCustomer));
    }

    /** Leaves straight away if nothing has been entered; otherwise asks first. */
    private void handleCancel() {
        if (!loanDetailsController.hasInput() || Dialogs.confirmDiscard(wizard, "Discard this new loan?",
                "Nothing has been sent to the mainframe yet, so anything entered will be lost.")) {
            navigator.back();
        }
    }

    private void handleSubmit() {
        // Later: create the loan through LoanService, then open it
        navigator.back();
    }
}
