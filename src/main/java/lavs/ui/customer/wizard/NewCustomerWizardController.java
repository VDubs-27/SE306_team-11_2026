package lavs.ui.customer.wizard;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

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
    @FXML private Node detailsStep;
    @FXML private Node addressesStep;
    @FXML private Node contactsStep;
    @FXML private Node employmentStep;
    @FXML private Node reviewStep;

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        wizardController.setTitle("New customer");
        wizardController.setSubtitle(NOT_SAVED);
        wizardController.setIcon(newCustomerIcon());
        wizardController.setFinishText("Save customer");
        wizardController.setOnCancel(this::handleCancel);
        wizardController.setOnFinish(this::handleSave);
        wizardController.setSteps(List.of(
                new WizardStep("Details", detailsStep),
                new WizardStep("Addresses", addressesStep),
                new WizardStep("Contacts", contactsStep),
                new WizardStep("Employment", employmentStep),
                new WizardStep("Review & save", reviewStep)));
    }

    private void handleCancel() {
        // Later: only ask when something has been entered
        if (Dialogs.confirm(wizard, "Discard this new customer?",
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
