package lavs.ui.customer;

import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

public class CustomerSearchController implements NavigatorAware {

    /** Temporary sample data for designing. Replaced by CustomerService results. */
    private static final List<CustomerRow> SAMPLE_CUSTOMERS = List.of(
            new CustomerRow("Mr John Smith", "01-0013-5810482", "14/03/1985"),
            new CustomerRow("Mr John Smith", "01-0013-5811967", "02/11/1962"),
            new CustomerRow("Mrs Sarah Smith", "01-0013-5810233", "21/07/1990"),
            new CustomerRow("Ms Aroha Smith", "01-0013-5812810", "09/01/1978"),
            new CustomerRow("Mr David Smith", "01-0013-5810775", "30/05/1971"),
            new CustomerRow("Ms Mei Smith", "01-0013-5813302", "17/09/1995"),
            new CustomerRow("Mr Peter Smithson", "01-0013-5811408", "03/12/1958"));

    private static final String PROMPT_MESSAGE =
            "Search by name or customer number to find a customer";
    private static final String NO_RESULTS_MESSAGE =
            "No customers found. Check the spelling or search by customer number.";

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Label resultsTitle;
    @FXML private Label resultCount;
    @FXML private TableView<CustomerRow> resultsTable;
    @FXML private TableColumn<CustomerRow, String> nameColumn;
    @FXML private TableColumn<CustomerRow, String> customerNumberColumn;
    @FXML private TableColumn<CustomerRow, String> dateOfBirthColumn;
    @FXML private TableColumn<CustomerRow, Void> arrowColumn;
    @FXML private Label emptyState;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().name()));
        customerNumberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().customerNumber()));
        dateOfBirthColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().dateOfBirth()));

        arrowColumn.setCellFactory(column -> new TableCell<>() {
            private final Label chevron = new Label("›");

            {
                chevron.getStyleClass().add("row-chevron");
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : chevron);
            }
        });

        resultsTable.setRowFactory(table -> {
            TableRow<CustomerRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    navigator.showCustomerProfile(row.getItem().customerNumber());
                }
            });
            return row;
        });

        showPrompt();
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) {
            showPrompt();
            return;
        }
        // Later: call CustomerService in the background instead of filtering samples
        showResults(term, filterSamples(term));
    }

    @FXML
    private void handleAddCustomer() {
        // Later: navigator.showNewCustomerWizard()
    }

    @FXML
    private void handlePrevPage() {
        // Later: load the previous page of results
    }

    @FXML
    private void handleNextPage() {
        // Later: load the next page of results
    }

    /** Starting state: nothing searched yet. */
    private void showPrompt() {
        resultsTitle.setText("Customers");
        resultCount.setText("");
        showMessage(PROMPT_MESSAGE);
        updatePaging(1, 1);
    }

    /** After a search: the table if there are matches, otherwise a message. */
    private void showResults(String term, List<CustomerRow> rows) {
        resultsTitle.setText("Search results for \u201C" + term + "\u201D");
        resultCount.setText(rows.size() == 1 ? "1 customer" : rows.size() + " customers");

        if (rows.isEmpty()) {
            showMessage(NO_RESULTS_MESSAGE);
        } else {
            resultsTable.getItems().setAll(rows);
            resultsTable.setVisible(true);
            emptyState.setVisible(false);
        }
        updatePaging(1, 1);
    }

    private void showMessage(String message) {
        resultsTable.getItems().clear();
        resultsTable.setVisible(false);
        emptyState.setText(message);
        emptyState.setVisible(true);
    }

    private void updatePaging(int currentPage, int totalPages) {
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);
    }

    /** Temporary: matches the search term against the sample data. */
    private List<CustomerRow> filterSamples(String term) {
        String lower = term.toLowerCase(Locale.ROOT);
        return SAMPLE_CUSTOMERS.stream()
                .filter(row -> row.name().toLowerCase(Locale.ROOT).contains(lower)
                        || row.customerNumber().contains(term))
                .toList();
    }
}