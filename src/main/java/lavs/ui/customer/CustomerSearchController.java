package lavs.ui.customer;

import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.service.Result;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.Nodes;
import lavs.ui.common.PagerController;
import lavs.ui.common.Tables;
import lavs.ui.common.Toasts;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;
import lavs.ui.shell.Refreshable;

/**
 * Customer search: find a customer by name or customer number, a page of results at a time, read
 * from the mainframe, and open their profile; or start a new customer.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class CustomerSearchController implements NavigatorAware, Refreshable {

    /** Corner radius of .card in app.css. */
    private static final double CARD_RADIUS = 16;

    // The mainframe matches whole names only, so the screen says how to search
    private static final String PROMPT_MESSAGE =
            "Search by a first or family name, e.g. Smith, or a full customer number, e.g. 01-0001-0000015";
    private static final String NO_RESULTS_MESSAGE =
            "No customers found. Check the spelling, and use whole names (Smith, not Smi) or a full customer number.";
    private static final String FAILED_MESSAGE = "The search didn't work. Try again.";

    @FXML private ScrollPane pageScroll;
    @FXML private VBox page;
    @FXML private VBox resultsCard;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label resultsTitle;
    @FXML private Label resultCount;
    @FXML private StackPane tableBox;
    @FXML private TableView<Customer.Summary> resultsTable;
    @FXML private TableColumn<Customer.Summary, String> nameColumn;
    @FXML private TableColumn<Customer.Summary, String> customerNumberColumn;
    @FXML private TableColumn<Customer.Summary, String> dateOfBirthColumn;
    @FXML private TableColumn<Customer.Summary, Void> arrowColumn;
    @FXML private Label emptyState;
    @FXML private VBox pagerSection;
    @FXML private PagerController pagerController;

    private Navigator navigator;
    /** True while results are showing, so Back has a page to read again. */
    private boolean searched;

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
        Nodes.fillViewport(pageScroll, page);
        Nodes.roundCorners(resultsCard, CARD_RADIUS);
        showPrompt();
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) {
            showPrompt();
            return;
        }
        searched = true;
        resultsTitle.setText("Search results for “" + term + "”");
        resultCount.setText("Searching…");
        searchButton.setDisable(true);
        pagerController.showLoading(page -> CustomerFinder.find(navigator.services().customer().customers(), term, page),
                rows -> Tables.show(resultsTable, tableBox, emptyState, rows), this::showFound);
    }

    /**
     * Back from a customer's profile: reads the page of results that was open again, so a customer
     * edited there shows as it is now.
     */
    @Override
    public void refresh() {
        if (searched) {
            pagerController.reload();
        }
    }

    /** Shows how many were found and whether there's more than a page, or why the search didn't work. */
    private void showFound(Result<Page<Customer.Summary>> found) {
        searchButton.setDisable(false);
        if (!found.successful()) {
            resultCount.setText("");
            Tables.show(resultsTable, tableBox, emptyState, List.of());
            emptyState.setText(FAILED_MESSAGE);
            Nodes.show(pagerSection, false);
            Toasts.show(searchField, Toasts.Kind.ERROR, "Couldn't search", found.errorMessage());
            return;
        }
        Page<Customer.Summary> page = found.value();
        resultCount.setText(page.total() == 1 ? "1 customer"
                : String.format(Locale.ROOT, "%d customers", page.total()));
        if (page.total() == 0) {
            emptyState.setText(NO_RESULTS_MESSAGE);
        }
        Nodes.show(pagerSection, page.totalPages() > 1);
    }

    @FXML
    private void handleAddCustomer() {
        navigator.showNewCustomerWizard();
    }

    /** Starting state: nothing searched yet. */
    private void showPrompt() {
        searched = false;
        pagerController.cancel();
        searchButton.setDisable(false);
        resultsTitle.setText("Customers");
        resultCount.setText("");
        Tables.show(resultsTable, tableBox, emptyState, List.of());
        emptyState.setText(PROMPT_MESSAGE);
        Nodes.show(pagerSection, false);
    }
}
