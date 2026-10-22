package lavs.ui.loan.people;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.service.Result;
import lavs.ui.common.Background;
import lavs.ui.common.Choices;
import lavs.ui.common.Nodes;

/**
 * The pop-up for adding an existing customer to a loan: choose co-borrower or guarantor, search the
 * mainframe, then click the person. People already on the loan are left out of the results.
 * AI-assisted: written by Jack, refined with Claude.
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
    private Function<String, Result<Page<Customer.Summary>>> find = term -> Result.success(new Page<>(List.of(), 1, 1, 0));
    /** Counts searches, so only the answer to the latest is shown. */
    private long searches;

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
        // Only the kinds of person the mainframe offers, the first chosen to start with
        List<Coborrower.Type> offered = Choices.options(
                ConstantCategory.COBORROWER_TYPE, Coborrower.Type.values(), Coborrower.Type.UNKNOWN);
        Nodes.show(coborrowerButton, offered.contains(Coborrower.Type.COBORROWER));
        Nodes.show(guarantorButton, offered.contains(Coborrower.Type.GUARANTOR));
        (offered.contains(Coborrower.Type.COBORROWER) ? coborrowerButton : guarantorButton).setSelected(true);
        searches++;
        searchField.clear();
        results.getChildren().clear();
        selected = null;
        showError(null);
    }

    /** How a search finds customers: the first page of matches for the term. Called in the background. */
    public void setFind(Function<String, Result<Page<Customer.Summary>>> find) {
        this.find = find;
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
        if (term.isEmpty()) {
            showMessage("Type a whole first or family name, or a full customer number.");
            return;
        }
        long search = ++searches;
        showMessage("Searching…");
        Background.run(() -> find.apply(term), found -> {
            if (search == searches) {
                showFound(found);
            }
        });
    }

    /** The matches not already on the loan, or why there are none. */
    private void showFound(Result<Page<Customer.Summary>> found) {
        if (!found.successful()) {
            showMessage("The search didn't work. " + found.errorMessage());
            return;
        }
        List<Customer> matches = found.value().items().stream()
                .filter(match -> !alreadyOnLoan.contains(match.number().value()))
                .map(FindCustomerController::customer)
                .toList();
        if (matches.isEmpty()) {
            showMessage("No customers found. Use whole names (Smith, not Smi) or a full customer number.");
            return;
        }
        results.getChildren().setAll(matches.stream().map(this::resultRow).toList());
        if (found.value().hasNext()) {
            Label more = new Label("Showing the first " + found.value().items().size() + " of "
                    + found.value().total() + ". Add a first name to narrow it down.");
            more.getStyleClass().add("text-muted");
            results.getChildren().add(more);
        }
    }

    private void showMessage(String text) {
        Label message = new Label(text);
        message.setWrapText(true);
        message.getStyleClass().add("text-muted");
        results.getChildren().setAll(message);
    }

    /** A search result as a customer, for its row; the mainframe is asked for the rest when they're added. */
    private static Customer customer(Customer.Summary found) {
        return new Customer(found.number(), found.title(), found.firstName(), found.familyName(), found.status(),
                found.dateOfBirth(), null);
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
        Nodes.show(selectionError, message != null);
    }
}
