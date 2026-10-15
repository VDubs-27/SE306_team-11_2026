package lavs.ui.loan.view;

import java.math.BigDecimal;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import lavs.logic.model.loan.Loan;
import lavs.logic.model.loan.ScheduledRepayment;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.PagerController;
import lavs.ui.common.Tables;

/**
 * The loan screen's Schedule tab: every repayment due over the life of the loan, split into
 * principal and interest, with the balance left after it, a page at a time.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class ScheduleTabController {

    /** A year of monthly repayments per page. */
    private static final long PAGE_SIZE = 12;

    @FXML private Label countLabel;
    @FXML private StackPane tableBox;
    @FXML private TableView<ScheduledRepayment> scheduleTable;
    @FXML private TableColumn<ScheduledRepayment, String> numberColumn;
    @FXML private TableColumn<ScheduledRepayment, String> dateColumn;
    @FXML private TableColumn<ScheduledRepayment, String> repaymentColumn;
    @FXML private TableColumn<ScheduledRepayment, String> principalColumn;
    @FXML private TableColumn<ScheduledRepayment, String> interestColumn;
    @FXML private TableColumn<ScheduledRepayment, String> balanceColumn;
    @FXML private Label emptyState;
    @FXML private PagerController pagerController;

    @FXML
    private void initialize() {
        numberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(String.valueOf(cell.getValue().paymentNumber())));
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(DisplayFormat.date(cell.getValue().date())));
        showMoney(repaymentColumn, ScheduledRepayment::amountPaid);
        showMoney(principalColumn, ScheduledRepayment::principalAmount);
        showMoney(interestColumn, ScheduledRepayment::interestAmount);
        showMoney(balanceColumn, ScheduledRepayment::amountRemaining);
        Tables.fitToRows(scheduleTable);
        Tables.fitInsideBox(scheduleTable);
    }

    /** Shows the first page of the loan's schedule. */
    void show(Loan loan) {
        // Later: page through LoanService's repayment schedule instead of working it out here
        long total = pagerController.show(page -> SampleLoans.schedulePage(loan, page, PAGE_SIZE),
                items -> Tables.show(scheduleTable, tableBox, emptyState, items));
        countLabel.setText(total == 1 ? "1 repayment" : total + " repayments");
    }

    private static void showMoney(TableColumn<ScheduledRepayment, String> column,
            Function<ScheduledRepayment, BigDecimal> amount) {
        column.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.moneyWithCents(amount.apply(cell.getValue()))));
    }
}
