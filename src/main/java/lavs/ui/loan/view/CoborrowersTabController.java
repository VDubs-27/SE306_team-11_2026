package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Dialogs;
import lavs.ui.common.Modal;
import lavs.ui.common.PagerController;
import lavs.ui.loan.people.FindCustomerController;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.loan.people.PersonRows;

/**
 * The loan screen's Co-borrowers tab: the applicant, then everyone else on the loan, a page at a
 * time. While the loan is New, people can be found and added, or removed; after that the list is locked.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CoborrowersTabController {

    private static final long PAGE_SIZE = 5;
    private static final String ADD = "add";

    @FXML private Node findCustomer;
    @FXML private FindCustomerController findCustomerController;
    @FXML private VBox peopleList;
    @FXML private PagerController pagerController;
    @FXML private Button findButton;
    @FXML private HBox errorBanner;
    @FXML private Label lockNote;

    private final List<LoanPerson> others = new ArrayList<>();
    private Loan loan;
    private LoanPerson applicant;
    private Runnable onChanged = () -> { };

    @FXML
    private void initialize() {
        show(errorBanner, false);
    }

    /** What happens after someone is added or removed: the loan screen redraws this tab and its count. */
    void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    /** The people on the loan when the screen opens: the applicant and everyone else. */
    void use(LoanPerson applicant, List<LoanPerson> others) {
        this.applicant = applicant;
        this.others.clear();
        this.others.addAll(others);
    }

    /** Shows everyone on the loan, with adding and removing only while it's New. Returns how many others there are. */
    long show(Loan loan) {
        this.loan = loan;
        boolean editable = isEditable();
        show(findButton, editable);
        lockNote.setText(editable
                ? "Add each person as a co-borrower or a guarantor. An applicant with no job needs a guarantor."
                : "This loan is " + loan.status().code().toLowerCase(Locale.ROOT)
                        + ", so who is on it can no longer be changed.");
        List<LoanPerson> people = Stream.concat(Stream.of(applicant), others.stream()).toList();
        pagerController.show(page -> SampleLoans.pageOf(people, page, PAGE_SIZE), this::draw);
        if (!editable || meetsGuarantorRule()) {
            show(errorBanner, false);
        }
        return others.size();
    }

    /** An applicant who isn't employed needs a guarantor before the loan is submitted; shows why if not. */
    boolean check() {
        boolean ok = meetsGuarantorRule();
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
            // Later: LoanService.addCoborrower(loan, ...); until then the change only shows on this screen
            others.add(LoanPerson.lookUp(findCustomerController.selected(), findCustomerController.role()));
            onChanged.run();
        }
    }

    private void handleRemove(LoanPerson person) {
        if (Dialogs.confirm(peopleList, "Remove " + person.customer().fullName() + " from this loan?",
                "They will no longer be on the loan. They stay a customer, and can be added again.", "Remove")) {
            // Later: LoanService.removeCoborrower(loan, ...); until then the change only shows on this screen
            others.remove(person);
            onChanged.run();
        }
    }

    /** One page of people; Remove is offered for everyone but the applicant while the loan is New. */
    private void draw(List<LoanPerson> page) {
        boolean editable = isEditable();
        peopleList.getChildren().setAll(page.stream()
                .map(person -> PersonRows.tile(person,
                        editable && !person.isApplicant() ? () -> handleRemove(person) : null))
                .toList());
    }

    private boolean isEditable() {
        return loan.status() == Loan.Status.NEW;
    }

    private boolean meetsGuarantorRule() {
        return applicant.employed() || others.stream().anyMatch(person -> person.role() == Coborrower.Type.GUARANTOR);
    }

    private Set<String> customerNumbersOnLoan() {
        return Stream.concat(Stream.of(applicant), others.stream())
                .map(person -> person.customer().number().value())
                .collect(Collectors.toSet());
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}
