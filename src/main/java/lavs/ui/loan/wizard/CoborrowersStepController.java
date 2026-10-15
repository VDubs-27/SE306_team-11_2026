package lavs.ui.loan.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.Modal;
import lavs.ui.loan.people.FindCustomerController;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.loan.people.PersonRows;

/**
 * Step 3 of the new loan wizard: who else is on the loan. The applicant is listed first; others
 * are found among existing customers or created through the new customer wizard.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CoborrowersStepController {

    private static final String ADD = "add";

    @FXML private Node findCustomer;
    @FXML private FindCustomerController findCustomerController;
    @FXML private VBox peopleList;
    @FXML private Label loanSummary;
    @FXML private HBox errorBanner;

    private NewLoanDraft draft;
    private Runnable onAddNewCustomer = () -> { };

    @FXML
    private void initialize() {
        show(errorBanner, false);
    }

    /** Gives the step the draft whose people it edits. */
    void use(NewLoanDraft draft) {
        this.draft = draft;
        refresh();
    }

    /** The loan this is for, in the heading: "$450,000 · Fixed · not yet submitted". */
    void showLoanSummary() {
        Loan.Details details = draft.details();
        // Later: show the loan number and status once submitting creates the loan
        loanSummary.setText(details == null
                ? "New loan · not yet submitted"
                : DisplayFormat.money(details.principal()) + " · "
                        + LoanText.rateTypeName(details.rateType())
                        + " · not yet submitted");
    }

    /** What "Add new customer" does: the wizard opens the new customer wizard. */
    void setOnAddNewCustomer(Runnable onAddNewCustomer) {
        this.onAddNewCustomer = onAddNewCustomer;
    }

    /** An applicant who isn't employed needs a guarantor; otherwise anyone may move on. */
    boolean check() {
        boolean ok = draft.applicant().employed() || hasGuarantor();
        show(errorBanner, !ok);
        return ok;
    }

    @FXML
    private void handleFindCustomer() {
        findCustomerController.reset(customerNumbersOnLoan());
        String chosen = new Modal(peopleList, "Find existing customer")
                .content(findCustomer)
                .button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(ADD, "Add to loan", Modal.Kind.PRIMARY, findCustomerController::check)
                .showAndWait();
        if (ADD.equals(chosen)) {
            draft.others().add(LoanPerson.lookUp(findCustomerController.selected(), findCustomerController.role()));
            refresh();
        }
    }

    @FXML
    private void handleAddNewCustomer() {
        // Later: once saving a new customer creates them, add them here as a co-borrower
        onAddNewCustomer.run();
    }

    private void handleRemove(LoanPerson person) {
        draft.others().remove(person);
        refresh();
    }

    private void refresh() {
        List<Node> rows = new ArrayList<>();
        rows.add(PersonRows.tile(draft.applicant(), null));
        for (LoanPerson person : draft.others()) {
            rows.add(PersonRows.tile(person, () -> handleRemove(person)));
        }
        peopleList.getChildren().setAll(rows);
        if (draft.applicant().employed() || hasGuarantor()) {
            show(errorBanner, false);
        }
    }

    private boolean hasGuarantor() {
        return draft.others().stream().anyMatch(person -> person.role() == Coborrower.Type.GUARANTOR);
    }

    private Set<String> customerNumbersOnLoan() {
        return Stream.concat(Stream.of(draft.applicant()), draft.others().stream())
                .map(person -> person.customer().number().value())
                .collect(Collectors.toSet());
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
