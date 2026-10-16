package lavs.ui.loan.view;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Background;
import lavs.ui.common.Dialogs;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.DocumentsTabController;
import lavs.ui.common.Fetch;
import lavs.ui.common.LoanText;
import lavs.ui.common.Nodes;
import lavs.ui.common.NotesTabController;
import lavs.ui.common.TabBars;
import lavs.ui.common.Toasts;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/**
 * The loan screen, opened from a customer's Loans tab: the loan's number, status and terms, its
 * totals, and tabs for its details, schedule, co-borrowers, documents and notes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanViewController implements NavigatorAware {

    /** Shown in a total's tile when the mainframe couldn't work it out. */
    private static final String NO_TOTAL = "—";

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
    @FXML private Tab scheduleTab;
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
    /** The loan's totals, or null when the mainframe couldn't give them. */
    private Loan.Totals totals;
    private LoanSources sources;
    /** What each tab does the first time it's opened: read its first page. */
    private final Map<Tab, Runnable> unopened = new HashMap<>();

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
        notesController.setAuthor(navigator.signedInName());
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
        tabPane.getSelectionModel().selectedItemProperty().addListener((property, was, tab) -> open(tab));
        detailsController.setOnSaved(changed -> sources.update(changed), this::showSaved);
        coborrowersController.setOnChanged(this::showCoborrowers);
    }

    /**
     * Fills the screen for one of a customer's loans, from what was read before opening (see
     * LoanLoader). The schedule, documents and notes are read when their tabs are first opened.
     */
    public void showLoan(LoanLoader.LoanScreen screen) {
        loan = screen.loan();
        customer = screen.applicant().customer();
        totals = screen.totals();
        sources = new LoanSources(navigator.services(), () -> loan, screen.coborrowerIds());
        coborrowersController.use(screen.applicant(), screen.others(), navigator.services().customer(),
                sources.people());
        TabBars.setCount(documentsTab, screen.documents());
        TabBars.setCount(notesTab, screen.notes());
        unopened.put(scheduleTab, this::showSchedule);
        unopened.put(documentsTab, () -> documentsController.show(sources.documents(),
                count -> TabBars.setCount(documentsTab, count)));
        unopened.put(notesTab, () -> notesController.show(sources.notes(),
                count -> TabBars.setCount(notesTab, count)));
        refresh();
        open(tabPane.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleBack() {
        navigator.back();
    }

    /**
     * Moves a New loan to Pending on the mainframe, after checking; its details are locked from then
     * on. A loan missing a guarantor it needs opens the Co-borrowers tab, which says why, instead; any
     * other reason it can't be submitted yet, such as no mailing address, is shown in a message.
     */
    @FXML
    private void handleSubmitForApproval() {
        if (!coborrowersController.check()) {
            tabPane.getSelectionModel().select(coborrowersTab);
            return;
        }
        if (!Dialogs.confirm(submitButton, "Submit this loan for approval?",
                "It will move to Pending, and its details can no longer be changed.", "Submit for approval")) {
            return;
        }
        submitButton.setDisable(true);
        Background.run(sources::submit, submitted -> {
            submitButton.setDisable(false);
            if (submitted.successful()) {
                loan = submitted.value();
                refresh();
                Toasts.show(submitButton, Toasts.Kind.SUCCESS, "Submitted for approval",
                        "Loan " + loan.loanNumber().value() + " is now pending.");
            } else if (submitted.errorMessage() == null) {
                Toasts.show(submitButton, Toasts.Kind.WARNING, "Not ready to submit", Fetch.reason(submitted));
            } else {
                Toasts.show(submitButton, Toasts.Kind.ERROR, "Couldn't submit the loan", submitted.errorMessage());
            }
        });
    }

    /** Reads a tab's first page the first time it's opened. */
    private void open(Tab tab) {
        Runnable firstOpen = unopened.remove(tab);
        if (firstOpen != null) {
            firstOpen.run();
        }
    }

    /** Shows the loan as saved from Edit; its totals and schedule are read again, as the terms changed. */
    private void showSaved(Loan saved) {
        loan = saved;
        refresh();
        if (!unopened.containsKey(scheduleTab)) {
            showSchedule();
        }
        Background.run(sources::totals, read -> {
            totals = read.value();
            showTotals(loan);
        });
    }

    private void showSchedule() {
        scheduleController.show(page -> sources.schedule(page, ScheduleTabController.pageSize()));
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
        showCoborrowers();
    }

    private void showCoborrowers() {
        TabBars.setCount(coborrowersTab, coborrowersController.show(loan));
    }

    /**
     * The four tiles: total to repay, total interest, the repayment, and the final payment. The
     * totals come from the mainframe; when it couldn't give them, those tiles show a dash.
     */
    private void showTotals(Loan loan) {
        repaymentValue.setText(DisplayFormat.moneyWithCents(loan.paymentAmount()));
        repaymentStartValue.setText(LoanText.paymentFrequencyName(loan.paymentFrequency()).toLowerCase(Locale.ROOT)
                + " from " + DisplayFormat.date(loan.startDate()));
        if (totals == null) {
            for (Label value : List.of(totalPaidValue, totalInterestValue, finalDateValue)) {
                value.setText(NO_TOTAL);
            }
            paymentCountValue.setText("");
            finalAmountValue.setText("");
            return;
        }
        totalPaidValue.setText(DisplayFormat.money(totals.totalAmountPaid()));
        paymentCountValue.setText("over " + totals.numberOfPayments() + " payments");
        totalInterestValue.setText(DisplayFormat.money(totals.totalInterestPaid()));
        finalDateValue.setText(DisplayFormat.date(totals.finalPaymentDate()));
        finalAmountValue.setText(totals.finalPaymentAmount() == null
                ? ""
                : DisplayFormat.moneyWithCents(totals.finalPaymentAmount()));
    }
}
