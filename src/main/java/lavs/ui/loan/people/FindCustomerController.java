package lavs.ui.loan.people;

import java.util.List;
import java.util.Set;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;
import lavs.ui.customer.profile.SampleProfiles;

/**
 * The pop-up for adding an existing customer to a loan: choose co-borrower or guarantor, search,
 * then click the person. People already on the loan are left out of the results.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class FindCustomerController {

    private static final String SELECTED = "person-result-selected";

    @FXML private ToggleGroup roleChoice;
    @FXML private ToggleButton coborrowerButton;
    @FXML private ToggleButton guarantorButton;
    @FXML private TextField searchField;
    @FXML private VBox results;
    @FXML private Label selectionError;

    private Set<String> alreadyOnLoan = Set.of();
    private Customer selected;

    @FXML
    private void initialize() {
        // One of the two is always chosen; clicking the chosen one again leaves it chosen
        roleChoice.selectedToggleProperty().addListener((obs, oldChoice, choice) -> {
            if (choice == null) {
                oldChoice.setSelected(true);
            }
        });
    }

    /** Starts afresh, leaving out the customer numbers already on the loan. */
    public void reset(Set<String> customerNumbersOnLoan) {
        alreadyOnLoan = customerNumbersOnLoan;
        coborrowerButton.setSelected(true);
        searchField.clear();
        results.getChildren().clear();
        selected = null;
        showError(null);
    }

    /** True once someone has been picked; otherwise says to pick someone. */
    public boolean check() {
        showError(selected == null ? "ⓘ  Search, then click the customer to add." : null);
        return selected != null;
    }

    public Customer selected() {
        return selected;
    }

    public Coborrower.Type role() {
        return guarantorButton.isSelected() ? Coborrower.Type.GUARANTOR : Coborrower.Type.COBORROWER;
    }

    @FXML
    private void handleSearch() {
        selected = null;
        String term = searchField.getText().trim();
        // Later: search through CustomerService instead of the sample profiles
        List<Customer> found = term.isEmpty() ? List.of() : SampleProfiles.search(term).stream()
                .filter(customer -> !alreadyOnLoan.contains(customer.number().value()))
                .toList();
        if (found.isEmpty()) {
            Label none = new Label(term.isEmpty() ? "Type a name or customer number to search." : "No customers found.");
            none.getStyleClass().add("text-muted");
            results.getChildren().setAll(none);
            return;
        }
        results.getChildren().setAll(found.stream().map(this::resultRow).toList());
    }

    /** A result the user can click to choose; the chosen one is highlighted. */
    private HBox resultRow(Customer customer) {
        HBox row = PersonRows.result(customer);
        row.setOnMouseClicked(event -> {
            results.getChildren().forEach(other -> other.getStyleClass().remove(SELECTED));
            row.getStyleClass().add(SELECTED);
            selected = customer;
            showError(null);
        });
        return row;
    }

    private void showError(String message) {
        selectionError.setText(message == null ? "" : message);
        selectionError.setVisible(message != null);
        selectionError.setManaged(message != null);
    }
}
