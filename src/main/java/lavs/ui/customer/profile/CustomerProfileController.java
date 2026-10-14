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

/** AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston. */
public class CustomerProfileController implements NavigatorAware {

    /** How many items each paged tab shows at once. */
    private static final int PAGE_SIZE = 5;

    /** Horizontal padding of the tab header area (app.css). */
    private static final double TAB_BAR_PADDING = 24;
    /** Horizontal padding inside each tab (app.css). */
    private static final double TAB_PADDING = 16;

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

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        tabPane.widthProperty().addListener((obs, oldWidth, width) -> spreadTabs(width.doubleValue()));
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, tab) -> sizeToTab(tab));
        sizeToTab(tabPane.getSelectionModel().getSelectedItem());
    }

    /**
     * A TabPane is as tall as its tallest tab, even when that tab isn't showing. Only the selected
     * tab's content counts towards the size, so the card fits whichever tab is open. Uses the tab
     * passed in, because each tab's own isSelected() hasn't caught up yet when selection changes.
     */
    private void sizeToTab(Tab selected) {
        for (Tab tab : tabPane.getTabs()) {
            tab.getContent().setManaged(tab == selected);
        }
    }

    /** Fills the screen for one customer. */
    public void showCustomer(String customerNumber) {
        // Later: load the profile from CustomerService in the background
        CustomerProfile profile = SampleProfiles.find(customerNumber);
        showSummary(profile);
        detailsController.show(profile.customer());
        setCount(addressesTab, addressesController.show(
                page -> SampleProfiles.pageOf(profile.addresses(), page, PAGE_SIZE)));
        setCount(contactsTab, contactsController.show(
                page -> SampleProfiles.pageOf(profile.phones(), page, PAGE_SIZE),
                page -> SampleProfiles.pageOf(profile.emails(), page, PAGE_SIZE)));
        setCount(employmentTab, employmentController.show(
                page -> SampleProfiles.pageOf(profile.employers(), page, PAGE_SIZE)));
        setCount(loansTab, loansController.show(
                page -> SampleProfiles.pageOf(profile.loans(), page, PAGE_SIZE)));
        setCount(documentsTab, documentsController.show(
                page -> SampleProfiles.pageOf(profile.documents(), page, PAGE_SIZE)));
        setCount(notesTab, notesController.show(
                page -> SampleProfiles.pageOf(profile.notes(), page, PAGE_SIZE)));
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    @FXML
    private void handleNewLoan() {
        // Later: navigator.showNewLoanWizard(customer)
    }

    /** The card at the top: avatar, name, status and key facts. */
    private void showSummary(CustomerProfile profile) {
        Customer customer = profile.customer();
        initialsAvatar.setText(initials(customer));
        nameLabel.setText(isBlank(customer.title())
                ? customer.fullName()
                : customer.title() + " " + customer.fullName());
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

    private static String initials(Customer customer) {
        return customer.firstName().substring(0, 1) + customer.familyName().substring(0, 1);
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

    /** Gives every tab the same width, so the bar fills the card and doesn't shift on selection. */
    private void spreadTabs(double width) {
        // Half a pixel spare, so rounding never pushes the last tab into the overflow menu
        double tabWidth = (width - TAB_BAR_PADDING - 0.5) / tabPane.getTabs().size() - TAB_PADDING;
        tabPane.setTabMinWidth(tabWidth);
        tabPane.setTabMaxWidth(tabWidth);
    }

    /** Puts a small count pill beside the tab name. */
    private static void setCount(Tab tab, long count) {
        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("tab-count");
        tab.setGraphic(countLabel);
    }
}
