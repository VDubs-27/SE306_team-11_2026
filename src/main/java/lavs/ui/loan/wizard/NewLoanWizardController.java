package lavs.ui.loan.wizard;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.Background;
import lavs.ui.common.Dialogs;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.Fetch;
import lavs.ui.common.Toasts;
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

    /** Sets the wizard up for the person the loan is for: their initials and "for John Smith · number". */
    public void start(LoanPerson applicant) {
        Customer customer = applicant.customer();
        Label avatar = new Label(DisplayFormat.initials(customer));
        avatar.getStyleClass().add("avatar");
        wizardController.setIcon(avatar);
        wizardController.setSubtitle("for " + customer.fullName() + " · " + customer.number().value());
        draft = new NewLoanDraft(applicant);
        uploadController.use(draft);
        uploadController.setOnChangeDetails(() -> wizardController.goBackTo(0));
        coborrowersController.use(draft, navigator.services().customer());
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

    /**
     * Sends the loan to the mainframe in the background and submits it for approval, then opens it.
     * If it couldn't be created, says why and stays, with everything entered still there.
     */
    private void handleSubmit() {
        wizardController.setBusy("Submitting…");
        Background.run(() -> LoanSaver.save(navigator.services().loan(), draft), saved -> {
            if (!saved.successful()) {
                wizardController.clearBusy();
                Toasts.show(wizard, Toasts.Kind.ERROR, "Couldn't create the loan", Fetch.reason(saved));
                return;
            }
            finish(saved.value());
        });
    }

    /** Leaves the wizard for the loan just made, saying what, if anything, still needs doing on it. */
    private void finish(LoanSaver.Saved saved) {
        Scene scene = wizard.getScene();
        String number = saved.loan().loanNumber().value();
        navigator.back();
        navigator.showLoan(saved.loan().loanNumber(), draft.applicant());
        if (saved.notSubmitted() != null) {
            Toasts.show(scene, Toasts.Kind.WARNING, "Loan " + number + " saved, but not submitted yet",
                    saved.notSubmitted() + " Fix this, then submit it from the loan. "
                            + String.join(" ", saved.notSaved()));
        } else if (!saved.notSaved().isEmpty()) {
            Toasts.show(scene, Toasts.Kind.WARNING, "Loan " + number + " submitted, but not everything was added",
                    "Add these from the loan. " + String.join(" ", saved.notSaved()));
        } else {
            Toasts.show(scene, Toasts.Kind.SUCCESS, "Loan submitted",
                    "Loan " + number + " for " + draft.applicant().customer().fullName()
                            + " has been sent for approval.");
        }
    }
}
