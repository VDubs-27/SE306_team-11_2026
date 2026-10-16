package lavs.ui.loan.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import lavs.logic.model.Page;
import lavs.logic.model.loan.ScheduledRepayment;
import lavs.logic.service.Result;
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
    private static final String NO_REPAYMENTS = "No repayments scheduled.";

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

    /** Shows the loan's schedule from the mainframe, a page at a time, read in the background with loadPage. */
    void show(LongFunction<Result<Page<ScheduledRepayment>>> loadPage) {
        countLabel.setText("");
        emptyState.setText("Loading…");
        Tables.show(scheduleTable, tableBox, emptyState, List.of());
        pagerController.showLoading(page -> loadPage.apply(page), items -> {
            emptyState.setText(NO_REPAYMENTS);
            Tables.show(scheduleTable, tableBox, emptyState, items);
        }, loaded -> {
            if (loaded.successful()) {
                long total = loaded.value().total();
                countLabel.setText(total == 1 ? "1 repayment" : total + " repayments");
            } else {
                emptyState.setText("Couldn't load the schedule. " + loaded.errorMessage());
            }
        });
    }

    /** How many repayments a page shows. */
    static long pageSize() {
        return PAGE_SIZE;
    }

    private static void showMoney(TableColumn<ScheduledRepayment, String> column,
            Function<ScheduledRepayment, BigDecimal> amount) {
        column.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.moneyWithCents(amount.apply(cell.getValue()))));
    }
}
