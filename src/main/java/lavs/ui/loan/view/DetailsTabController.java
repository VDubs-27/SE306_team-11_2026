package lavs.ui.loan.view;

import java.util.function.Consumer;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.logic.service.Result;
import lavs.ui.common.DisplayFormat;
import lavs.ui.common.LoanText;
import lavs.ui.common.Nodes;
import lavs.ui.common.RecordEditor;
import lavs.ui.loan.forms.LoanDetailsFormController;

/**
 * The loan screen's Details tab: the loan's terms, read-only. While the loan is New they can be
 * changed with Edit, which opens the same form as the new loan wizard; after that they're locked.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DetailsTabController {

    @FXML private Node detailsForm;
    @FXML private LoanDetailsFormController detailsFormController;
    @FXML private Button editButton;
    @FXML private GridPane detailGrid;
    @FXML private Label lockNote;

    private Loan loan;
    private RecordEditor<Loan.Details> editor;
    private Function<Loan.Details, Result<Loan>> save = details -> Result.success(loan);
    private Consumer<Loan> onSaved = saved -> { };

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(detailGrid, "loan details", detailsForm, detailsFormController).withoutRemove();
    }

    /**
     * How Edit saves: save stores the change, in the background, and says whether it worked (the pop-up
     * stays open if not); onSaved then gets the saved loan, to show.
     */
    void setOnSaved(Function<Loan.Details, Result<Loan>> save, Consumer<Loan> onSaved) {
        this.save = save;
        this.onSaved = onSaved;
    }

    /** Shows the loan's details, with Edit only while it's New. */
    void show(Loan loan) {
        this.loan = loan;
        Loan.Details details = loan.details();
        detailGrid.getChildren().clear();
        add(0, 0, "Loan number", loan.loanNumber().value());
        add(1, 0, "Status", loan.status().code());
        add(0, 1, "Principal", DisplayFormat.money(details.principal()));
        add(1, 1, "Rate type", LoanText.rateTypeName(details.rateType()));
        add(0, 2, "Interest rate", details.rateAmount().stripTrailingZeros().toPlainString() + "%");
        add(1, 2, "Rate applies", details.rateFrequency().code());
        add(0, 3, "Repayment amount", DisplayFormat.moneyWithCents(details.paymentAmount()));
        add(1, 3, "Repayment frequency", LoanText.paymentFrequencyName(details.paymentFrequency()));
        add(0, 4, "Start date", DisplayFormat.date(details.startDate()));
        add(1, 4, "Renewal date", DisplayFormat.date(details.renewalDate()));

        boolean editable = LoanLock.isEditable(loan);
        Nodes.show(editButton, editable);
        lockNote.setText(editable
                ? "These details can be changed until the loan is submitted for approval."
                : LoanLock.lockedNote(loan, "its details"));
    }

    @FXML
    private void handleEdit() {
        editor.editAndSave(loan.details(), save, onSaved, () -> Result.success(null));
    }

    /** One read-only field: a small label with its value under it. */
    private void add(int column, int row, String name, String value) {
        Label label = new Label(name);
        label.getStyleClass().add("detail-label");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("detail-value");
        VBox field = new VBox(label, valueLabel);
        field.getStyleClass().add("detail");
        detailGrid.add(field, column, row);
    }
}
