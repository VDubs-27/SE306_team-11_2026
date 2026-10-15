package lavs.ui.customer.profile;

import java.util.function.Consumer;
import java.util.function.LongFunction;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import lavs.logic.model.Page;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.common.PagerController;
import lavs.ui.common.Tables;

/**
 * The Loans tab: a table of the customer's loans, a page at a time.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoansTabController {

    @FXML private StackPane tableBox;
    @FXML private TableView<Loan> loansTable;
    @FXML private TableColumn<Loan, String> loanNumberColumn;
    @FXML private TableColumn<Loan, String> principalColumn;
    @FXML private TableColumn<Loan, String> repaymentColumn;
    @FXML private TableColumn<Loan, Loan.Status> statusColumn;
    @FXML private TableColumn<Loan, Void> arrowColumn;
    @FXML private Label emptyState;
    @FXML private PagerController pagerController;

    private Consumer<Loan> onOpenLoan = loan -> { };

    @FXML
    private void initialize() {
        loanNumberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().loanNumber().value()));
        principalColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(DisplayFormat.money(cell.getValue().principal())));
        repaymentColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(LoanText.repayment(cell.getValue().details())));
        statusColumn.setCellValueFactory(
                cell -> new SimpleObjectProperty<>(cell.getValue().status()));

        loanNumberColumn.setCellFactory(column -> new TableCell<>() {
            {
                getStyleClass().add("mono");
            }

            @Override
            protected void updateItem(String number, boolean empty) {
                super.updateItem(number, empty);
                setText(empty ? null : number);
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Loan.Status status, boolean empty) {
                super.updateItem(status, empty);
                setGraphic(empty || status == null ? null : LoanText.statusBadge(status));
            }
        });

        Tables.makeRowsOpenable(loansTable, arrowColumn, this::handleOpenLoan);
        Tables.fitInsideBox(loansTable);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<Loan>> loadPage) {
        return pagerController.show(loadPage, items -> Tables.show(loansTable, tableBox, emptyState, items));
    }

    /** What clicking a loan does: the profile opens the loan screen. */
    void setOnOpenLoan(Consumer<Loan> onOpenLoan) {
        this.onOpenLoan = onOpenLoan;
    }

    private void handleOpenLoan(Loan loan) {
        onOpenLoan.accept(loan);
    }
}
