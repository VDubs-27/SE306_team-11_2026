package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.service.Result;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentsTabController;
import lavs.ui.common.Nodes;
import lavs.ui.common.NotesTabController;
import lavs.ui.common.Pages;
import lavs.ui.common.TabBars;
import lavs.ui.common.Toasts;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/** AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston. */
public class CustomerProfileController implements NavigatorAware {

    /** How many items each paged tab shows at once. */
    private static final int PAGE_SIZE = 5;

    @FXML private Label initialsAvatar;
    @FXML private Label nameLabel;
    @FXML private Label statusBadge;
    @FXML private Label customerNumberLabel;
    @FXML private Label dateOfBirthLabel;
    @FXML private Label suburbLabel;
    @FXML private HBox dateOfBirthFact;
    @FXML private HBox suburbFact;
    @FXML private TabPane tabPane;
    @FXML private Tab addressesTab;
    @FXML private Tab contactsTab;
    @FXML private Tab employmentTab;
    @FXML private Tab loansTab;
    @FXML private Tab documentsTab;
    @FXML private Tab notesTab;
    @FXML private DetailsTabController detailsController;
    @FXML private AddressesTabController addressesController;
    @FXML private ContactsTabController contactsController;
    @FXML private EmploymentTabController employmentController;
    @FXML private LoansTabController loansController;
    @FXML private DocumentsTabController documentsController;
    @FXML private NotesTabController notesController;

    private Customer customer;
    private String suburb;
    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
        loansController.setOnOpenLoan(loan -> navigator.showLoan(loan, customer));
        detailsController.setOnSaved(this::saveDetails);
        detailsController.setOnStatusChanged(this::changeStatus);
        // Later: save document and note changes through CustomerService; until then they only show on this screen
        documentsController.setOnChanged(() -> TabBars.setCount(documentsTab, documentsController.count()));
        notesController.setOnChanged(() -> TabBars.setCount(notesTab, notesController.count()));
    }

    /** Fills the screen for one customer. */
    public void showCustomer(String customerNumber) {
        // Later: load the profile from CustomerService in the background; if that fails,
        // navigator.showLoadFailed("Couldn't load this customer", reason, () -> showCustomer(customerNumber))
        CustomerProfile profile = SampleProfiles.find(customerNumber);
        customer = profile.customer();
        suburb = primarySuburb(profile);
        showSummary();
        detailsController.show(customer);
        TabBars.setCount(addressesTab, addressesController.show(
                page -> Pages.of(profile.addresses(), page, PAGE_SIZE)));
        TabBars.setCount(contactsTab, contactsController.show(
                page -> Pages.of(profile.phones(), page, PAGE_SIZE),
                page -> Pages.of(profile.emails(), page, PAGE_SIZE)));
        TabBars.setCount(employmentTab, employmentController.show(
                page -> Pages.of(profile.employers(), page, PAGE_SIZE)));
        TabBars.setCount(loansTab, loansController.show(
                page -> Pages.of(profile.loans(), page, PAGE_SIZE)));
        documentsController.use(profile.documents());
        TabBars.setCount(documentsTab, documentsController.count());
        notesController.use(profile.notes());
        TabBars.setCount(notesTab, notesController.count());
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    @FXML
    private void handleNewLoan() {
        navigator.showNewLoanWizard(customer);
    }

    /** Saves the edited details, keeping the customer's number and status; the edit pop-up shows any failure. */
    private Result<?> saveDetails(Customer.Details changed) {
        // Later: Result<?> saved = customerService.update(customer.number(), changed); if it failed, return it
        customer = new Customer(customer.number(), changed.title(), changed.firstName(), changed.familyName(),
                customer.status(), changed.dateOfBirth(), changed.nationality());
        showSummary();
        detailsController.show(customer);
        Toasts.show(nameLabel, Toasts.Kind.SUCCESS, "Details saved",
                DisplayFormat.fullNameWithTitle(customer) + "'s details have been updated.");
        return Result.success(null);
    }

    /** Makes the customer active or inactive, once confirmed on the Details tab. */
    private void changeStatus(Customer.Status status) {
        // Later: CustomerService.updateStatus(customer.number(), status); until then the change only shows on this screen
        customer = customer.withStatus(status);
        showSummary();
        detailsController.show(customer);
        Toasts.show(nameLabel, Toasts.Kind.SUCCESS, "Status changed",
                DisplayFormat.fullNameWithTitle(customer) + " is now " + status.code().toLowerCase(Locale.ROOT) + ".");
    }

    /** The card at the top: avatar, name, status and key facts. */
    private void showSummary() {
        initialsAvatar.setText(DisplayFormat.initials(customer));
        nameLabel.setText(DisplayFormat.fullNameWithTitle(customer));
        customerNumberLabel.setText(customer.number().value());

        statusBadge.setText(customer.status().code());
        statusBadge.getStyleClass().removeAll("badge-active", "badge-neutral");
        statusBadge.getStyleClass().add(
                customer.status() == Customer.Status.ACTIVE ? "badge-active" : "badge-neutral");
        showIfPresent(statusBadge, statusBadge);

        dateOfBirthLabel.setText(customer.dateOfBirth() == null ? "" : DisplayFormat.date(customer.dateOfBirth()));
        showIfPresent(dateOfBirthLabel, dateOfBirthFact);

        suburbLabel.setText(suburb);
        showIfPresent(suburbLabel, suburbFact);
    }

    private static String primarySuburb(CustomerProfile profile) {
        return profile.addresses().stream()
                .filter(Address::isPrimary)
                .map(Address::suburb)
                .filter(suburb -> !isBlank(suburb))
                .findFirst()
                .orElse("");
    }

    /** Hides what holds a label (the label itself, or its fact and divider) when it has no text, leaving no gap. */
    private static void showIfPresent(Label label, Node holder) {
        Nodes.show(holder, !isBlank(label.getText()));
    }
}
