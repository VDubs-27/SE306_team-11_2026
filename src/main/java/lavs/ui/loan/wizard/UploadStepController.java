package lavs.ui.loan.wizard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.ScheduledRepayment;
import lavs.logic.service.LoanService;
import lavs.ui.common.Background;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.common.Modal;
import lavs.ui.common.Nodes;
import lavs.ui.common.Tables;
import lavs.ui.loan.view.ScheduleTabController;

/**
 * Step 2 of the new loan wizard: the loan as sent to the mainframe in step 1, with its number and
 * status, a summary with its totals, and its first repayments, to go through with the customer. The
 * whole schedule opens in a pop-up. Going back to step 1 sends any changes as an update to the same loan.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class UploadStepController {

    /** Shown for a total while it's read, or when the mainframe couldn't work it out. */
    private static final String NO_TOTAL = "—";
    /** How many repayments the step shows before "Show full schedule". */
    private static final long PREVIEW_ROWS = 5;

    @FXML private Label loanNumberLabel;
    @FXML private Label statusLabel;
    @FXML private Label repaymentCaption;
    @FXML private Label repaymentValue;
    @FXML private Label principalValue;
    @FXML private Label termValue;
    @FXML private Label totalInterestValue;
    @FXML private Label totalPaidValue;
    @FXML private Label termsLabel;
    @FXML private Label previewCountLabel;
    @FXML private StackPane previewBox;
    @FXML private TableView<ScheduledRepayment> previewTable;
    @FXML private TableColumn<ScheduledRepayment, String> numberColumn;
    @FXML private TableColumn<ScheduledRepayment, String> dateColumn;
    @FXML private TableColumn<ScheduledRepayment, String> paymentColumn;
    @FXML private TableColumn<ScheduledRepayment, String> principalColumn;
    @FXML private TableColumn<ScheduledRepayment, String> interestColumn;
    @FXML private TableColumn<ScheduledRepayment, String> balanceColumn;
    @FXML private Label previewEmpty;
    @FXML private Button fullScheduleButton;
    @FXML private Node fullSchedule;
    @FXML private ScheduleTabController scheduleController;

    private NewLoanDraft draft;
    private LoanService loans;

    @FXML
    private void initialize() {
        numberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(String.valueOf(cell.getValue().paymentNumber())));
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(DisplayFormat.date(cell.getValue().date())));
        showMoney(paymentColumn, ScheduledRepayment::amountPaid);
        showMoney(principalColumn, ScheduledRepayment::principalAmount);
        showMoney(interestColumn, ScheduledRepayment::interestAmount);
        showMoney(balanceColumn, ScheduledRepayment::amountRemaining);
        Tables.fitToRows(previewTable);
    }

    /** Gives the step the draft whose loan it shows. */
    void use(NewLoanDraft draft) {
        this.draft = draft;
    }

    /**
     * Shows the loan just sent, then reads its totals and first repayments from the mainframe in the
     * background; runs each time the step shows, as the details may have changed.
     */
    void show(LoanService loans) {
        this.loans = loans;
        Loan loan = draft.loan();
        loanNumberLabel.setText(loan.loanNumber().value());
        statusLabel.setText(" created with status " + loan.status().code()
                + ". You can still edit it until it is submitted.");
        repaymentCaption.setText(LoanText.paymentFrequencyName(loan.paymentFrequency()) + " repayment");
        repaymentValue.setText(DisplayFormat.moneyWithCents(loan.paymentAmount()));
        principalValue.setText(DisplayFormat.money(loan.principal()));

        showTotals(loan, null);
        Background.run(() -> loans.totals(loan.loanNumber()), read -> {
            // Only if the loan wasn't changed again while they were read
            if (loan == draft.loan()) {
                showTotals(loan, read.successful() ? read.value() : null);
            }
        });

        previewCountLabel.setText("");
        previewEmpty.setText("Loading…");
        Nodes.show(fullScheduleButton, false);
        Tables.show(previewTable, previewBox, previewEmpty, List.of());
        Background.run(() -> loans.schedule(loan.loanNumber(), 1, PREVIEW_ROWS), read -> {
            if (loan != draft.loan()) {
                return;
            }
            if (!read.successful()) {
                previewEmpty.setText("Couldn't load the schedule. " + read.errorMessage());
                return;
            }
            List<ScheduledRepayment> first = read.value().items();
            long total = read.value().total();
            previewEmpty.setText("No repayments scheduled.");
            Tables.show(previewTable, previewBox, previewEmpty, first);
            previewCountLabel.setText(first.isEmpty() ? "" : "Showing " + first.size() + " of " + total);
            Nodes.show(fullScheduleButton, total > first.size());
        });
    }

    /** The summary's totals, or a dash for each when there are none (yet). */
    private void showTotals(Loan loan, Loan.Totals totals) {
        if (totals == null) {
            termValue.setText(NO_TOTAL);
            totalInterestValue.setText(NO_TOTAL);
            totalPaidValue.setText(NO_TOTAL);
            termsLabel.setText(LoanText.interest(loan.details()));
            return;
        }
        termValue.setText(term(loan.startDate(), totals.finalPaymentDate()));
        totalInterestValue.setText(DisplayFormat.money(totals.totalInterestPaid()));
        totalPaidValue.setText(DisplayFormat.money(totals.totalAmountPaid()));
        termsLabel.setText(LoanText.interest(loan.details()) + " · payoff date "
                + DisplayFormat.date(totals.finalPaymentDate()));
    }

    /** How long the loan runs, e.g. "30 years", or "9 months" when it's under a year. */
    private static String term(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return NO_TOTAL;
        }
        long months = ChronoUnit.MONTHS.between(start, end);
        if (months < 12) {
            return months == 1 ? "1 month" : months + " months";
        }
        long years = Math.round(months / 12.0);
        return years == 1 ? "1 year" : years + " years";
    }

    /** Opens every repayment, a page at a time, in a pop-up. */
    @FXML
    private void handleShowFullSchedule() {
        Loan loan = draft.loan();
        scheduleController.show(page -> loans.schedule(loan.loanNumber(), page, ScheduleTabController.pageSize()));
        new Modal(fullScheduleButton, "Loan " + loan.loanNumber().value())
                .content(fullSchedule)
                .button("close", "Close", Modal.Kind.SECONDARY)
                .showAndWait();
    }

    private static void showMoney(TableColumn<ScheduledRepayment, String> column,
            Function<ScheduledRepayment, BigDecimal> amount) {
        column.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.moneyWithCents(amount.apply(cell.getValue()))));
    }
}
