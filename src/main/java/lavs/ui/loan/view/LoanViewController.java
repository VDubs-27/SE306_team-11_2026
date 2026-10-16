package lavs.ui.loan.view;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Dialogs;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentsTabController;
import lavs.ui.common.LoanText;
import lavs.ui.common.Nodes;
import lavs.ui.common.NotesTabController;
import lavs.ui.common.TabBars;
import lavs.ui.loan.people.LoanPerson;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/**
 * The loan screen, opened from a customer's Loans tab: the loan's number, status and terms, its
 * totals, and tabs for its details, schedule, co-borrowers, documents and notes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanViewController implements NavigatorAware {

    @FXML private Hyperlink backLink;
    @FXML private Label loanNumberLabel;
    @FXML private Label statusBadge;
    @FXML private Label customerValue;
    @FXML private Label principalValue;
    @FXML private Label interestValue;
    @FXML private Button submitButton;
    @FXML private Label totalPaidValue;
    @FXML private Label paymentCountValue;
    @FXML private Label totalInterestValue;
    @FXML private Label repaymentValue;
    @FXML private Label repaymentStartValue;
    @FXML private Label finalDateValue;
    @FXML private Label finalAmountValue;
    @FXML private TabPane tabPane;
    @FXML private Tab coborrowersTab;
    @FXML private Tab documentsTab;
    @FXML private Tab notesTab;
    @FXML private DetailsTabController detailsController;
    @FXML private ScheduleTabController scheduleController;
    @FXML private CoborrowersTabController coborrowersController;
    @FXML private DocumentsTabController documentsController;
    @FXML private NotesTabController notesController;

    private Navigator navigator;
    private Loan loan;
    private Customer customer;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
        notesController.setAuthor(navigator.signedInName());
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
        detailsController.setOnSaved(this::saveDetails);
        coborrowersController.setOnChanged(this::showCoborrowers);
        // Later: save document and note changes through LoanService; until then they only show on this screen
        documentsController.setOnChanged(this::showCounts);
        notesController.setOnChanged(this::showCounts);
    }

    /** Fills the screen for one of a customer's loans; applicant is the customer it's for. */
    public void showLoan(Loan loan, LoanPerson applicant) {
        this.loan = loan;
        this.customer = applicant.customer();
        coborrowersController.use(applicant, SampleLoans.coborrowers(loan));
        documentsController.use(SampleLoans.documents(loan));
        notesController.use(SampleLoans.notes(loan));
        refresh();
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    /**
     * Moves a New loan to Pending, after checking; its details are locked from then on. A loan
     * missing a guarantor it needs opens the Co-borrowers tab, which says why, instead.
     */
    @FXML
    private void handleSubmitForApproval() {
        if (!coborrowersController.check()) {
            tabPane.getSelectionModel().select(coborrowersTab);
            return;
        }
        if (Dialogs.confirm(submitButton, "Submit this loan for approval?",
                "It will move to Pending, and its details can no longer be changed.", "Submit for approval")) {
            // Later: LoanService.updateStatus(loan, PENDING); until then the change only shows on this screen
            loan = loan.withStatus(Loan.Status.PENDING);
            refresh();
        }
    }

    /** Keeps the loan's number, customer and status, with the edited terms. */
    private void saveDetails(Loan.Details changed) {
        // Later: LoanService.update(loan, changed); until then the change only shows on this screen
        loan = new Loan(loan.loanNumber(), loan.customerNumber(), loan.status(), changed.principal(),
                changed.rateType(), changed.rateAmount(), changed.rateFrequency(), changed.startDate(),
                changed.paymentAmount(), changed.paymentFrequency(), changed.renewalDate());
        refresh();
    }

    /** Shows the loan as it is now: summary, totals and every tab. */
    private void refresh() {
        Loan.Details details = loan.details();
        backLink.setText("‹  Back to " + customer.fullName());
        loanNumberLabel.setText(loan.loanNumber().value());
        Label badge = LoanText.statusBadge(loan.status());
        statusBadge.setText(badge.getText());
        statusBadge.getStyleClass().setAll(badge.getStyleClass());
        customerValue.setText("for " + customer.fullName());
        principalValue.setText(DisplayFormat.money(loan.principal()));
        interestValue.setText(LoanText.interest(details));
        // Offered only when the loan's status allows the move, which the model decides (New to Pending)
        Nodes.show(submitButton, loan.status().canMoveTo(Loan.Status.PENDING));
        showTotals(loan);
        detailsController.show(loan);
        scheduleController.show(loan);
        showCoborrowers();
        showCounts();
    }

    private void showCoborrowers() {
        TabBars.setCount(coborrowersTab, coborrowersController.show(loan));
    }

    private void showCounts() {
        TabBars.setCount(documentsTab, documentsController.count());
        TabBars.setCount(notesTab, notesController.count());
    }

    /** The four tiles: total to repay, total interest, the repayment, and the final payment. */
    private void showTotals(Loan loan) {
        // Later: read these from LoanService (the mainframe's loan summary) instead of working them out
        Loan.Totals totals = SampleLoans.totals(loan);
        totalPaidValue.setText(DisplayFormat.money(totals.totalAmountPaid()));
        paymentCountValue.setText("over " + totals.numberOfPayments() + " payments");
        totalInterestValue.setText(DisplayFormat.money(totals.totalInterestPaid()));
        repaymentValue.setText(DisplayFormat.moneyWithCents(loan.paymentAmount()));
        repaymentStartValue.setText(LoanText.paymentFrequencyName(loan.paymentFrequency()).toLowerCase(Locale.ROOT)
                + " from " + DisplayFormat.date(loan.startDate()));
        finalDateValue.setText(DisplayFormat.date(totals.finalPaymentDate()));
        finalAmountValue.setText(totals.finalPaymentAmount() == null
                ? ""
                : DisplayFormat.moneyWithCents(totals.finalPaymentAmount()));
    }
}
