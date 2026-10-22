package lavs.ui.customer.wizard;

import java.util.List;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.ui.common.Background;
import lavs.ui.common.Dialogs;
import lavs.ui.common.Fetch;
import lavs.ui.common.Toasts;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;
import lavs.ui.wizard.WizardController;
import lavs.ui.wizard.WizardStep;

/**
 * The new customer wizard: details, addresses, contacts, employment, then review and save. Opened
 * from another screen (such as the loan wizard), saving goes back there with the new customer.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class NewCustomerWizardController implements NavigatorAware {

    private static final String NOT_SAVED = "Not yet saved to the mainframe";

    @FXML private Node wizard;
    @FXML private WizardController wizardController;
    @FXML private Node details;
    @FXML private DetailsStepController detailsController;
    @FXML private Node addresses;
    @FXML private AddressesStepController addressesController;
    @FXML private Node contacts;
    @FXML private ContactsStepController contactsController;
    @FXML private Node employment;
    @FXML private EmploymentStepController employmentController;
    @FXML private Node review;
    @FXML private ReviewStepController reviewController;

    private final NewCustomerDraft draft = new NewCustomerDraft();
    private Navigator navigator;
    private Consumer<NewCustomer> onSaved;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    /** What happens once the customer is saved, back on the screen that opened the wizard. */
    public void setOnSaved(Consumer<NewCustomer> action) {
        this.onSaved = action;
    }

    @FXML
    private void initialize() {
        addressesController.use(draft);
        contactsController.use(draft);
        employmentController.use(draft);
        wizardController.setTitle("New customer");
        wizardController.setSubtitle(NOT_SAVED);
        wizardController.setIcon(newCustomerIcon());
        wizardController.setFinishText("Save customer");
        wizardController.setOnCancel(this::handleCancel);
        wizardController.setOnFinish(this::handleSave);
        wizardController.setSteps(List.of(
                new WizardStep("Details", details, this::checkDetails, null),
                new WizardStep("Addresses", addresses, addressesController::check, null),
                new WizardStep("Contacts", contacts, contactsController::check, null),
                new WizardStep("Employment", employment, employmentController::check, null),
                new WizardStep("Review & save", review, null, () -> reviewController.show(draft))));
    }

    /** Checks the details and, once they pass, puts the customer's name in the header. */
    private boolean checkDetails() {
        if (!detailsController.check(draft)) {
            return false;
        }
        Customer.Details entered = draft.details();
        wizardController.setSubtitle(entered.firstName() + " " + entered.familyName() + " · not yet saved to the mainframe");
        return true;
    }

    /** Leaves straight away if nothing has been entered; otherwise asks first. */
    private void handleCancel() {
        if (!detailsController.hasInput() || Dialogs.confirmDiscard(wizard, "Discard this new customer?",
                "Nothing has been saved to the mainframe yet, so anything entered will be lost.")) {
            navigator.back();
        }
    }

    /**
     * Saves the customer to the mainframe in the background, then goes back: to the screen waiting for
     * them, if there is one, or to their new profile. If they couldn't be created, says why and stays.
     */
    private void handleSave() {
        wizardController.setBusy("Saving…");
        Background.run(() -> CustomerSaver.save(navigator.services().customer(), draft), saved -> {
            if (!saved.successful()) {
                wizardController.clearBusy();
                Toasts.show(wizard, Toasts.Kind.ERROR, "Couldn't save the customer", Fetch.reason(saved));
                return;
            }
            finish(saved.value());
        });
    }

    /** Leaves the wizard for the customer just saved, saying what, if anything, didn't save with them. */
    private void finish(CustomerSaver.Saved saved) {
        Customer customer = saved.customer();
        Scene scene = wizard.getScene();
        if (onSaved != null) {
            navigator.back();
            onSaved.accept(new NewCustomer(customer, primarySuburb(), draft.isEmployed() && !draft.employers().isEmpty()));
        } else {
            navigator.showCustomerProfileInstead(customer.number().value());
        }
        if (saved.notSaved().isEmpty()) {
            Toasts.show(scene, Toasts.Kind.SUCCESS, "Customer saved", customer.fullName() + " has been added.");
        } else {
            Toasts.show(scene, Toasts.Kind.WARNING, customer.fullName() + " was saved, but not all their details",
                    "Add these again from their profile. " + String.join(" ", saved.notSaved()));
        }
    }

    private String primarySuburb() {
        return draft.addresses().stream()
                .filter(Address.Details::isPrimary)
                .map(Address.Details::suburb)
                .findFirst()
                .orElse(null);
    }

    /** A person with a plus, in a dashed circle. */
    private static StackPane newCustomerIcon() {
        SVGPath person = new SVGPath();
        person.setContent("M6 7a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M1 15c0-3 2.2-5 5-5s5 2 5 5 M13 3v6 M10 6h6");
        person.getStyleClass().add("wizard-icon-glyph");
        StackPane icon = new StackPane(person);
        icon.getStyleClass().add("wizard-icon");
        return icon;
    }
}
