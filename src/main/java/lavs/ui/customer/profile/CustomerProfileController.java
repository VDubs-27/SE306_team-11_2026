package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.Background;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentsTabController;
import lavs.ui.common.Nodes;
import lavs.ui.common.Fetch;
import lavs.ui.common.NotesTabController;
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

    private CustomerProfile profile;
    private Customer customer;
    /** What each tab does the first time it's opened: read its first page of records. */
    private final Map<Tab, Runnable> unopened = new HashMap<>();
    /** True while checking the customer can have a loan, so a second click doesn't open two wizards. */
    private boolean checkingLoan;
    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
        tabPane.getSelectionModel().selectedItemProperty().addListener((property, was, tab) -> open(tab));
        loansController.setOnOpenLoan(loan -> navigator.showLoan(loan, profile.asApplicant(customer)));
        detailsController.setOnSaved(
                changed -> navigator.services().customer().customers().update(customer.number(), changed),
                this::showSavedDetails);
        detailsController.setOnStatusChanged(this::changeStatus);
    }

    /**
     * Fills the screen for one customer, from what was read before opening (see ProfileLoader). Each
     * tab reads its own records, a page at a time, when it's first opened.
     */
    public void showCustomer(CustomerProfile loaded) {
        profile = loaded;
        customer = profile.customer();
        showSummary();
        detailsController.show(customer);
        TabBars.setCount(addressesTab, profile.addresses());
        TabBars.setCount(contactsTab, profile.contacts());
        TabBars.setCount(employmentTab, profile.employers());
        TabBars.setCount(loansTab, profile.loans());
        TabBars.setCount(documentsTab, profile.documents());
        TabBars.setCount(notesTab, profile.notes());

        ProfileSources sources = new ProfileSources(navigator.services(), customer.number());
        unopened.put(addressesTab, () -> addressesController.show(sources.addresses(),
                count -> TabBars.setCount(addressesTab, count), this::refreshSuburb));
        unopened.put(contactsTab, () -> contactsController.show(sources.phones(), sources.emails(),
                count -> TabBars.setCount(contactsTab, count)));
        unopened.put(employmentTab, () -> employmentController.show(sources.employers(), count -> {
            TabBars.setCount(employmentTab, count);
            profile = profile.withEmployers(count);
        }));
        unopened.put(loansTab, () -> loansController.show(page -> sources.loans(page, PAGE_SIZE),
                count -> TabBars.setCount(loansTab, count)));
        unopened.put(documentsTab, () -> documentsController.show(sources.documents(),
                count -> TabBars.setCount(documentsTab, count)));
        unopened.put(notesTab, () -> notesController.show(sources.notes(),
                count -> TabBars.setCount(notesTab, count)));
        open(tabPane.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    /**
     * Opens the new loan wizard once the mainframe confirms the customer can have one: they're active,
     * with a mailing address and a way to reach them. Otherwise says what's missing.
     */
    @FXML
    private void handleNewLoan() {
        if (checkingLoan) {
            return;
        }
        checkingLoan = true;
        Background.run(() -> navigator.services().loan().loans().canStartLoan(customer.number()), ready -> {
            checkingLoan = false;
            if (ready.successful()) {
                navigator.showNewLoanWizard(profile.asApplicant(customer));
            } else if (ready.errorMessage() == null) {
                Toasts.show(nameLabel, Toasts.Kind.WARNING, "Not ready for a loan yet", Fetch.reason(ready));
            } else {
                Toasts.show(nameLabel, Toasts.Kind.ERROR, "Couldn't check for a new loan", ready.errorMessage());
            }
        });
    }

    /** Reads a tab's first page of records the first time it's opened. */
    private void open(Tab tab) {
        Runnable firstOpen = unopened.remove(tab);
        if (firstOpen != null) {
            firstOpen.run();
        }
    }

    /** Shows the customer as saved, once the edit pop-up has closed. */
    private void showSavedDetails(Customer saved) {
        customer = saved;
        showSummary();
        detailsController.show(customer);
        Toasts.show(nameLabel, Toasts.Kind.SUCCESS, "Details saved",
                DisplayFormat.fullNameWithTitle(customer) + "'s details have been updated.");
    }

    /** Makes the customer active or inactive on the mainframe, once confirmed on the Details tab. */
    private void changeStatus(Customer.Status status) {
        Background.run(() -> navigator.services().customer().customers().changeStatus(customer, status), changed -> {
            if (!changed.successful()) {
                Toasts.show(nameLabel, Toasts.Kind.ERROR, "Couldn't change the status", Fetch.reason(changed));
                return;
            }
            customer = changed.value();
            showSummary();
            detailsController.show(customer);
            Toasts.show(nameLabel, Toasts.Kind.SUCCESS, "Status changed", DisplayFormat.fullNameWithTitle(customer)
                    + " is now " + status.code().toLowerCase(Locale.ROOT) + ".");
        });
    }

    /** Reads the primary address's suburb again after the addresses changed, for the card at the top. */
    private void refreshSuburb() {
        Background.run(() -> ProfileLoader.primarySuburb(navigator.services().customer(), customer.number()), suburb -> {
            if (suburb.successful()) {
                profile = profile.withPrimarySuburb(suburb.value());
                showSummary();
            }
        });
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

        suburbLabel.setText(profile.primarySuburb());
        showIfPresent(suburbLabel, suburbFact);
    }

    /** Hides what holds a label (the label itself, or its fact and divider) when it has no text, leaving no gap. */
    private static void showIfPresent(Label label, Node holder) {
        Nodes.show(holder, !isBlank(label.getText()));
    }
}
