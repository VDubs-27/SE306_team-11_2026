package lavs.ui.customer.profile;

import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import lavs.logic.model.loan.Loan;

/** The Loans tab: a table of the customer's loans. */
public class LoansTabController {

    /** Row height from .table-row-cell in app.css. */
    private static final double ROW_HEIGHT = 52;
    /** Column header row plus the table's 1px border top and bottom, with a pixel spare. */
    private static final double HEADER_HEIGHT = 46;

    @FXML private TableView<Loan.Summary> loansTable;
    @FXML private TableColumn<Loan.Summary, String> loanNumberColumn;
    @FXML private TableColumn<Loan.Summary, Loan.Status> statusColumn;
    @FXML private TableColumn<Loan.Summary, String> principalColumn;
    @FXML private TableColumn<Loan.Summary, String> startDateColumn;
    @FXML private TableColumn<Loan.Summary, Void> arrowColumn;
    @FXML private Label emptyState;

    @FXML
    private void initialize() {
        loanNumberColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().loanNumber().value()));
        statusColumn.setCellValueFactory(
                cell -> new SimpleObjectProperty<>(cell.getValue().status()));
        principalColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.money(cell.getValue().principal())));
        startDateColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.date(cell.getValue().startDate())));

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

        arrowColumn.setCellFactory(column -> new TableCell<>() {
            private final Label chevron = new Label("›");

            {
                chevron.getStyleClass().add("row-chevron");
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : chevron);
            }
        });

        loansTable.setRowFactory(table -> {
            TableRow<Loan.Summary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    handleOpenLoan(row.getItem());
                }
            });
            return row;
        });
        loansTable.setFixedCellSize(ROW_HEIGHT);
    }

    void show(List<Loan.Summary> loans) {
        loansTable.getItems().setAll(loans);
        // Just tall enough for the rows, so short lists don't leave a block of empty table
        loansTable.setPrefHeight(HEADER_HEIGHT + loans.size() * ROW_HEIGHT);

        boolean hasLoans = !loans.isEmpty();
        loansTable.setVisible(hasLoans);
        loansTable.setManaged(hasLoans);
        emptyState.setVisible(!hasLoans);
        emptyState.setManaged(!hasLoans);
    }

    private void handleOpenLoan(Loan.Summary loan) {
        // Later: navigator.showLoan(loan.loanNumber())
    }

    private static Label statusBadge(Loan.Status status) {
        return RecordTiles.badge(status.code(), switch (status) {
            case ACTIVE -> "badge-active";
            case DEFAULTED -> "badge-error";
            case NEW, PENDING -> "badge-mailing";
            default -> "badge-neutral";
        });
    }
}
