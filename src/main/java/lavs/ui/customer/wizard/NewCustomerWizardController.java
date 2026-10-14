package lavs.ui.customer.wizard;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.Dialogs;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;
import lavs.ui.wizard.WizardController;
import lavs.ui.wizard.WizardStep;

/**
 * The new customer wizard: details, addresses, contacts, employment, then review and save.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
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
    @FXML private Node employmentStep;
    @FXML private Node reviewStep;

    private final NewCustomerDraft draft = new NewCustomerDraft();
    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        addressesController.use(draft);
        contactsController.use(draft);
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
                new WizardStep("Employment", employmentStep),
                new WizardStep("Review & save", reviewStep)));
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
        if (!detailsController.hasInput() || Dialogs.confirm(wizard, "Discard this new customer?",
                "Nothing has been saved to the mainframe yet, so anything entered will be lost.", "Discard")) {
            navigator.back();
        }
    }

    private void handleSave() {
        // Later: create the customer through CustomerService, then open their profile
        navigator.back();
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
