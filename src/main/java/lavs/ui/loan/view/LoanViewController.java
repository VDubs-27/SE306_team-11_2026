package lavs.ui.loan.view;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.common.TabBars;
import lavs.ui.shell.Navigator;
import lavs.ui.shell.NavigatorAware;

/**
 * The loan screen, opened from a customer's Loans tab: the loan's number, status and terms, its
 * totals, and tabs for its details, schedule, payments, co-borrowers, documents and notes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanViewController implements NavigatorAware {

    @FXML private Hyperlink backLink;
    @FXML private Label loanNumberLabel;
    @FXML private Label statusBadge;
    @FXML private Label customerLabel;
    @FXML private Label termsLabel;
    @FXML private Label totalPaidValue;
    @FXML private Label paymentCountValue;
    @FXML private Label totalInterestValue;
    @FXML private Label repaymentValue;
    @FXML private Label repaymentStartValue;
    @FXML private Label finalDateValue;
    @FXML private Label finalAmountValue;
    @FXML private TabPane tabPane;

    private Navigator navigator;

    @Override
    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        TabBars.setUp(tabPane);
    }

    /** Fills the screen for one of a customer's loans. */
    public void showLoan(Loan loan, Customer customer) {
        Loan.Details details = loan.details();
        backLink.setText("‹  Back to " + customer.fullName());
        loanNumberLabel.setText(loan.loanNumber().value());
        Label badge = LoanText.statusBadge(loan.status());
        statusBadge.setText(badge.getText());
        statusBadge.getStyleClass().setAll(badge.getStyleClass());
        customerLabel.setText("for " + customer.fullName() + " · " + customer.number().value());
        termsLabel.setText(DisplayFormat.money(loan.principal()) + " · " + LoanText.interest(details));
        showTotals(loan);
    }

    @FXML
    private void handleBack() {
        navigator.back();
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
