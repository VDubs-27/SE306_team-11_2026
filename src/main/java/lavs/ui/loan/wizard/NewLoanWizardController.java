package lavs.ui.loan.wizard;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.DisplayFormat;
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
    @FXML private Node loanDetailsStep;
    @FXML private Node uploadStep;
    @FXML private Node coborrowers;
    @FXML private CoborrowersStepController coborrowersController;
    @FXML private Node submitStep;

    private Navigator navigator;

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
                new WizardStep("Loan details", loanDetailsStep),
                new WizardStep("Upload & review", uploadStep),
                new WizardStep("Co-borrowers", coborrowers, coborrowersController::check, null),
                new WizardStep("Submit", submitStep)));
    }

    /** Sets the wizard up for the customer the loan is for: their initials and "for John Smith · number". */
    public void start(Customer customer) {
        Label avatar = new Label(DisplayFormat.initials(customer));
        avatar.getStyleClass().add("avatar");
        wizardController.setIcon(avatar);
        wizardController.setSubtitle("for " + customer.fullName() + " · " + customer.number().value());
        NewLoanDraft draft = new NewLoanDraft(LoanPerson.lookUp(customer, null));
        coborrowersController.use(draft);
        coborrowersController.setOnAddNewCustomer(navigator::showNewCustomerWizard);
    }

    private void handleCancel() {
        // Later: once the loan details step exists, ask before discarding anything entered
        navigator.back();
    }

    private void handleSubmit() {
        // Later: create the loan through LoanService, then open it
        navigator.back();
    }
}
