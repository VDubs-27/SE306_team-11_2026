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
 * AI-assisted: written by Jack, refined with Claude.
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
                new WizardStep("Loan details", loanDetails,
                        () -> loanDetailsController.check(draft) && sendLoan(), null),
                new WizardStep("Upload & review", upload, null,
                        () -> uploadController.show(navigator.services().loan().loans())),
                new WizardStep("Co-borrowers", coborrowers, coborrowersController::check,
                        () -> coborrowersController.showLoanSummary()),
                new WizardStep("Submit", submit, null, () -> submitController.show(draft))));
        wizardController.setBackText(1, "Edit loan details");
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
        coborrowersController.use(draft, navigator.services().customer());
        coborrowersController.setOnAddNewCustomer(
                () -> navigator.showNewCustomerWizard(coborrowersController::addNewCustomer));
    }

    /**
     * Leaves straight away if nothing has been entered; otherwise asks first. Once the loan has been
     * sent it stays on the mainframe as New, so the question says where to find it.
     */
    private void handleCancel() {
        if (draft.loan() != null) {
            if (Dialogs.confirm(wizard, "Leave this new loan?", "Loan " + draft.loan().loanNumber().value()
                    + " is already saved as New and can be finished from " + draft.applicant().customer().fullName()
                    + "'s loans. Co-borrowers not added yet will be lost.", "Leave")) {
                navigator.back();
            }
        } else if (!loanDetailsController.hasInput() || Dialogs.confirmDiscard(wizard, "Discard this new loan?",
                "Nothing has been sent to the mainframe yet, so anything entered will be lost.")) {
            navigator.back();
        }
    }

    /**
     * Sends the loan details to the mainframe in the background, creating the loan the first time and
     * updating it after that, then moves on. If they're turned down, stays and says why. Returns false,
     * as moving on waits for the mainframe.
     */
    private boolean sendLoan() {
        wizardController.setBusy("Sending to mainframe…");
        Background.run(() -> LoanSaver.send(navigator.services().loan(), draft), sent -> {
            wizardController.clearBusy();
            if (sent.successful()) {
                draft.setLoan(sent.value());
                wizardController.advance();
                return;
            }
            String reason = loanDetailsController.showProblems(sent);
            if (reason != null) {
                Toasts.show(wizard, Toasts.Kind.ERROR, draft.loan() == null
                        ? "Couldn't create the loan" : "Couldn't update the loan", reason);
            }
        });
        return false;
    }

    /**
     * Adds the people to the loan in the background and submits it for approval, then opens it.
     * If that couldn't run at all, says why and stays, with everything entered still there.
     */
    private void handleSubmit() {
        wizardController.setBusy("Submitting…");
        Background.run(() -> LoanSaver.save(navigator.services().loan(), draft), saved -> {
            if (!saved.successful()) {
                wizardController.clearBusy();
                Toasts.show(wizard, Toasts.Kind.ERROR, "Couldn't submit the loan", Fetch.reason(saved));
                return;
            }
            finish(saved.value());
        });
    }

    /** Leaves the wizard for the loan just made, saying what, if anything, still needs doing on it. */
    private void finish(LoanSaver.Saved saved) {
        Scene scene = wizard.getScene();
        String number = saved.loan().loanNumber().value();
        navigator.showLoanInstead(saved.loan().loanNumber(), draft.applicant());
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
