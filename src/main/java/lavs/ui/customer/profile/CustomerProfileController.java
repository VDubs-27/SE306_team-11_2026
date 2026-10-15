package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.TabBars;

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
    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
        loansController.setOnOpenLoan(loan -> navigator.showLoan(loan, customer));
    }

    /** Fills the screen for one customer. */
    public void showCustomer(String customerNumber) {
        // Later: load the profile from CustomerService in the background
        CustomerProfile profile = SampleProfiles.find(customerNumber);
        customer = profile.customer();
        showSummary(profile);
        detailsController.show(profile.customer());
        TabBars.setCount(addressesTab, addressesController.show(
                page -> SampleProfiles.pageOf(profile.addresses(), page, PAGE_SIZE)));
        TabBars.setCount(contactsTab, contactsController.show(
                page -> SampleProfiles.pageOf(profile.phones(), page, PAGE_SIZE),
                page -> SampleProfiles.pageOf(profile.emails(), page, PAGE_SIZE)));
        TabBars.setCount(employmentTab, employmentController.show(
                page -> SampleProfiles.pageOf(profile.employers(), page, PAGE_SIZE)));
        TabBars.setCount(loansTab, loansController.show(
                page -> SampleProfiles.pageOf(profile.loans(), page, PAGE_SIZE)));
        TabBars.setCount(documentsTab, documentsController.show(
                page -> SampleProfiles.pageOf(profile.documents(), page, PAGE_SIZE)));
        TabBars.setCount(notesTab, notesController.show(
                page -> SampleProfiles.pageOf(profile.notes(), page, PAGE_SIZE)));
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    @FXML
    private void handleNewLoan() {
        navigator.showNewLoanWizard(customer);
    }

    /** The card at the top: avatar, name, status and key facts. */
    private void showSummary(CustomerProfile profile) {
        Customer customer = profile.customer();
        initialsAvatar.setText(DisplayFormat.initials(customer));
        nameLabel.setText(DisplayFormat.fullNameWithTitle(customer));
        customerNumberLabel.setText(customer.number().value());

        statusBadge.setText(customer.status().code());
        statusBadge.getStyleClass().add(
                customer.status() == Customer.Status.ACTIVE ? "badge-active" : "badge-neutral");
        showIfPresent(statusBadge);

        dateOfBirthLabel.setText(customer.dateOfBirth() == null
                ? ""
                : "Born " + DisplayFormat.date(customer.dateOfBirth()));
        showIfPresent(dateOfBirthLabel);

        suburbLabel.setText(primarySuburb(profile));
        showIfPresent(suburbLabel);
    }

    private static String primarySuburb(CustomerProfile profile) {
        return profile.addresses().stream()
                .filter(Address::isPrimary)
                .map(Address::suburb)
                .filter(suburb -> !isBlank(suburb))
                .findFirst()
                .orElse("");
    }

    /** Hides a label with no text so it leaves no gap. */
    private static void showIfPresent(Label label) {
        boolean present = !isBlank(label.getText());
        label.setVisible(present);
        label.setManaged(present);
    }
}
