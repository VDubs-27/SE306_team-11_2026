package lavs.ui.loan.people;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Coborrower;
import lavs.ui.common.Dialogs;
import lavs.ui.common.Modal;
import lavs.ui.common.Nodes;
import lavs.ui.common.PagerController;
import lavs.ui.common.Pages;

/**
 * Who is on a loan, used by the new loan wizard and the loan screen: the applicant first, then
 * each co-borrower and guarantor, a page at a time. People are found among existing customers and
 * removed after a check. An applicant who isn't employed needs a guarantor.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanPeopleController {

    private static final long PAGE_SIZE = 5;
    private static final String ADD = "add";

    @FXML private Node findCustomer;
    @FXML private FindCustomerController findCustomerController;
    @FXML private VBox peopleList;
    @FXML private PagerController pagerController;
    @FXML private HBox addButtons;
    @FXML private Button newCustomerButton;
    @FXML private HBox errorBanner;

    private LoanPerson applicant;
    private List<LoanPerson> others = List.of();
    private boolean editable = true;
    private Runnable onAddNewCustomer;
    private Runnable onChanged = () -> { };

    @FXML
    private void initialize() {
        Nodes.show(errorBanner, false);
        Nodes.show(newCustomerButton, false);
    }

    /** The applicant, and everyone else on the loan; this list is changed in place as people are added or removed. */
    public void use(LoanPerson loanApplicant, List<LoanPerson> otherPeople) {
        this.applicant = loanApplicant;
        this.others = otherPeople;
        redraw();
    }

    /** Whether people can be added and removed; when not, the list is shown read-only. */
    public void setEditable(boolean canEdit) {
        this.editable = canEdit;
        Nodes.show(addButtons, canEdit);
        redraw();
    }

    /** Offers "Add new customer" beside "Find existing customer", which runs this. */
    public void setOnAddNewCustomer(Runnable action) {
        this.onAddNewCustomer = action;
        Nodes.show(newCustomerButton, true);
    }

    /** What happens after someone is added or removed, e.g. the screen updates a count. */
    public void setOnChanged(Runnable action) {
        this.onChanged = action;
    }

    /** How many people are on the loan besides the applicant. */
    public long count() {
        return others.size();
    }

    /** An applicant who isn't employed needs a guarantor; shows why when that's missing. */
    public boolean check() {
        boolean ok = meetsGuarantorRule();
        Nodes.show(errorBanner, !ok);
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
            others.add(LoanPerson.lookUp(findCustomerController.selected(), findCustomerController.role()));
            changed();
        }
    }

    @FXML
    private void handleAddNewCustomer() {
        // Later: once saving a new customer creates them, add them here as a co-borrower
        onAddNewCustomer.run();
    }

    private void handleRemove(LoanPerson person) {
        if (Dialogs.confirm(peopleList, "Remove " + person.customer().fullName() + " from this loan?",
                "They will no longer be on the loan. They stay a customer, and can be added again.", "Remove")) {
            others.remove(person);
            changed();
        }
    }

    private void changed() {
        redraw();
        onChanged.run();
    }

    /** Shows everyone from the first page; the warning goes once the guarantor rule is met. */
    private void redraw() {
        if (applicant == null) {
            return;
        }
        List<LoanPerson> people = Stream.concat(Stream.of(applicant), others.stream()).toList();
        pagerController.show(page -> Pages.of(people, page, PAGE_SIZE), this::draw);
        if (!editable || meetsGuarantorRule()) {
            Nodes.show(errorBanner, false);
        }
    }

    /** One page of people; Remove is offered for everyone but the applicant while the list can change. */
    private void draw(List<LoanPerson> page) {
        peopleList.getChildren().setAll(page.stream()
                .map(person -> PersonRows.tile(person,
                        editable && !person.isApplicant() ? () -> handleRemove(person) : null))
                .toList());
    }

    private boolean meetsGuarantorRule() {
        return applicant.employed() || others.stream().anyMatch(person -> person.role() == Coborrower.Type.GUARANTOR);
    }

    private Set<String> customerNumbersOnLoan() {
        return Stream.concat(Stream.of(applicant), others.stream())
                .map(person -> person.customer().number().value())
                .collect(Collectors.toSet());
    }
}
