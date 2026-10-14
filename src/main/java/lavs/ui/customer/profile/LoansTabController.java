package lavs.ui.customer.profile;

import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;

import lavs.logic.model.loan.Loan;

/** The Loans tab: a table of the customer's loans. */
public class LoansTabController {

    @FXML private StackPane tableBox;
    @FXML private TableView<Loan> loansTable;
    @FXML private TableColumn<Loan, String> loanNumberColumn;
    @FXML private TableColumn<Loan, String> principalColumn;
    @FXML private TableColumn<Loan, String> repaymentColumn;
    @FXML private TableColumn<Loan, Loan.Status> statusColumn;
    @FXML private TableColumn<Loan, Void> arrowColumn;
    @FXML private Label emptyState;

    @FXML
    private void initialize() {
        loanNumberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().loanNumber().value()));
        principalColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.money(cell.getValue().principal())));
        repaymentColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(repayment(cell.getValue())));
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
                setGraphic(empty || status == null ? null : statusBadge(status));
            }
        });

        ProfileTables.makeRowsOpenable(loansTable, arrowColumn, this::handleOpenLoan);
        ProfileTables.fitInsideBox(loansTable);
    }

    void show(List<Loan> loans) {
        ProfileTables.show(loansTable, tableBox, emptyState, loans);
    }

    private void handleOpenLoan(Loan loan) {
        // Later: navigator.showLoan(loan.loanNumber())
    }

    /** e.g. "$1,312.14 fortnightly". */
    private static String repayment(Loan loan) {
        if (loan.paymentAmount() == null) {
            return ProfileFormat.NOT_RECORDED;
        }
        String amount = ProfileFormat.moneyWithCents(loan.paymentAmount());
        return switch (loan.paymentFrequency()) {
            case ON_RENEWAL -> amount + " on renewal";
            case UNKNOWN -> amount;
            default -> amount + " " + loan.paymentFrequency().code().toLowerCase(Locale.ROOT);
        };
    }

    private static Label statusBadge(Loan.Status status) {
        return RecordTiles.badge(status.code(), switch (status) {
            case ACTIVE -> "badge-active";
            case DEFAULTED -> "badge-error";
            case NEW, PENDING -> "badge-warning";
            default -> "badge-neutral";
        });
    }
}
