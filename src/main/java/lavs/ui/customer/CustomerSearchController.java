package lavs.ui.customer;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Customer;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.Nodes;
import lavs.ui.common.PagerController;
import lavs.ui.common.Pages;
import lavs.ui.common.Tables;
import lavs.ui.customer.profile.SampleProfiles;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/**
 * Customer search: find a customer by name or customer number, a page of results at a time, and
 * open their profile; or start a new customer.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CustomerSearchController implements NavigatorAware {

    /** Results shown at once; a search returning more is paged. */
    private static final long PAGE_SIZE = 10;

    private static final String PROMPT_MESSAGE =
            "Search by name or customer number to find a customer";
    private static final String NO_RESULTS_MESSAGE =
            "No customers found. Check the spelling or search by customer number.";

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label resultsTitle;
    @FXML private Label resultCount;
    @FXML private StackPane tableBox;
    @FXML private TableView<Customer> resultsTable;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> customerNumberColumn;
    @FXML private TableColumn<Customer, String> dateOfBirthColumn;
    @FXML private TableColumn<Customer, Void> arrowColumn;
    @FXML private Label emptyState;
    @FXML private VBox pagerSection;
    @FXML private PagerController pagerController;

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.fullNameWithTitle(cell.getValue())));
        customerNumberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().number().value()));
        dateOfBirthColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.date(cell.getValue().dateOfBirth())));
        // Clicking a result opens that customer's profile
        Tables.makeRowsOpenable(resultsTable, arrowColumn,
                customer -> navigator.showCustomerProfile(customer.number().value()));
        showPrompt();
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) {
            showPrompt();
            return;
        }
        // Later: ask CustomerService for each page in the background instead of filtering the samples
        List<Customer> found = SampleProfiles.search(term);
        resultsTitle.setText("Search results for “" + term + "”");
        resultCount.setText(found.size() == 1 ? "1 customer" : found.size() + " customers");
        long total = pagerController.show(page -> Pages.of(found, page, PAGE_SIZE),
                rows -> Tables.show(resultsTable, tableBox, emptyState, rows));
        if (total == 0) {
            emptyState.setText(NO_RESULTS_MESSAGE);
        }
        Nodes.show(pagerSection, total > PAGE_SIZE);
    }

    @FXML
    private void handleAddCustomer() {
        navigator.showNewCustomerWizard();
    }

    /** Starting state: nothing searched yet. */
    private void showPrompt() {
        resultsTitle.setText("Customers");
        resultCount.setText("");
        Tables.show(resultsTable, tableBox, emptyState, List.of());
        emptyState.setText(PROMPT_MESSAGE);
        Nodes.show(pagerSection, false);
    }
}
