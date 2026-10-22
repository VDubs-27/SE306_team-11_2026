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

import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.service.Result;
import lavs.ui.common.Background;
import lavs.ui.common.Choices;
import lavs.ui.common.Dialogs;
import lavs.ui.common.Fetch;
import lavs.ui.common.Modal;
import lavs.ui.common.Nodes;
import lavs.ui.common.PagerController;
import lavs.ui.common.Pages;
import lavs.ui.common.Toasts;
import lavs.ui.customer.CustomerFinder;
import lavs.ui.shell.CustomerServices;

/**
 * Who is on a loan, used by the new loan wizard and the loan screen: the applicant first, then
 * each co-borrower and guarantor, a page at a time. People are found among existing customers on the
 * mainframe and removed after a check; on a loan already made, each change is saved to it straight
 * away (see useServices). An applicant who isn't employed needs a guarantor.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class LoanPeopleController {

    private static final long PAGE_SIZE = 5;
    private static final String ADD = "add";

    /** How adding and removing someone is kept; each runs in the background. */
    public interface Saving {
        Result<?> add(LoanPerson person);

        Result<?> remove(LoanPerson person);
    }

    /** Changes only kept on screen, e.g. in a wizard's draft, saved with the rest later. */
    public static final Saving ON_SCREEN = new Saving() {
        @Override
        public Result<?> add(LoanPerson person) {
            return Result.success(person);
        }

        @Override
        public Result<?> remove(LoanPerson person) {
            return Result.success(person);
        }
    };

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
    private CustomerServices customers;
    private Saving saving = ON_SCREEN;

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

    /** Where people are found and read from, and how adding or removing them is saved. */
    public void useServices(CustomerServices customerServices, Saving changes) {
        this.customers = customerServices;
        this.saving = changes;
        findCustomerController.setFind(term -> CustomerFinder.find(customerServices.customers(), term, 1));
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
        Modal modal = new Modal(peopleList, "Find existing customer").content(findCustomer);
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(ADD, "Add to loan", Modal.Kind.PRIMARY, () -> findCustomerController.check() && adding(modal))
                .showAndWait();
    }

    /**
     * Reads the chosen customer's details and saves them onto the loan, in the background with the
     * pop-up waiting; closes it once they're added, or says why they couldn't be. Returns false, so
     * the button doesn't close the pop-up itself.
     */
    private boolean adding(Modal modal) {
        Customer chosen = findCustomerController.selected();
        Coborrower.Type role = findCustomerController.role();
        modal.showError(null);
        modal.setBusy(ADD, "Adding…");
        Background.run(() -> Fetch.run(() -> {
            LoanPerson person = Fetch.value(PersonLoader.load(customers, chosen.number(), role));
            Fetch.value(saving.add(person));
            return person;
        }), added -> {
            modal.clearBusy();
            if (!added.successful()) {
                modal.showError(Fetch.reason(added));
                return;
            }
            modal.close(ADD);
            others.add(added.value());
            changed();
        });
        return false;
    }

    @FXML
    private void handleAddNewCustomer() {
        onAddNewCustomer.run();
    }

    /**
     * Adds a customer just made in the new customer wizard, asking first whether they're a
     * co-borrower or a guarantor (when the mainframe offers both).
     */
    public void addNewCustomer(Customer customer, String suburb, boolean employed) {
        Coborrower.Type role = askRole(customer);
        if (role == null) {
            return;
        }
        others.add(new LoanPerson(customer, role, suburb, employed));
        changed();
        Toasts.show(peopleList, Toasts.Kind.SUCCESS, "Added to the loan", customer.fullName() + " is on the loan as a "
                + (role == Coborrower.Type.GUARANTOR ? "guarantor." : "co-borrower."));
    }

    /** Co-borrower or guarantor; null if the pop-up is closed without choosing. */
    private Coborrower.Type askRole(Customer customer) {
        List<Coborrower.Type> offered = Choices.options(
                ConstantCategory.COBORROWER_TYPE, Coborrower.Type.values(), Coborrower.Type.UNKNOWN);
        if (offered.size() == 1) {
            return offered.get(0);
        }
        String chosen = new Modal(peopleList, "Add " + customer.fullName() + " to the loan")
                .message("A co-borrower shares the loan; a guarantor promises to repay it if the borrowers can't.")
                .button("guarantor", "Guarantor", Modal.Kind.SECONDARY)
                .button("coborrower", "Co-borrower", Modal.Kind.PRIMARY)
                .showAndWait();
        return switch (chosen) {
            case "coborrower" -> Coborrower.Type.COBORROWER;
            case "guarantor" -> Coborrower.Type.GUARANTOR;
            default -> null;
        };
    }

    private void handleRemove(LoanPerson person) {
        if (!Dialogs.confirm(peopleList, "Remove " + person.customer().fullName() + " from this loan?",
                "They will no longer be on the loan. They stay a customer, and can be added again.", "Remove")) {
            return;
        }
        Background.run(() -> Fetch.run(() -> Fetch.value(saving.remove(person))), removed -> {
            if (removed.successful()) {
                others.remove(person);
                changed();
            } else {
                Toasts.show(peopleList, Toasts.Kind.ERROR, "Couldn't remove " + person.customer().fullName(),
                        Fetch.reason(removed));
            }
        });
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
