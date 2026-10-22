package lavs.ui.loan.forms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import lavs.logic.model.FieldError;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Choices;
import lavs.ui.common.DateInput;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.LoanText;
import lavs.ui.common.Numbers;
import lavs.ui.common.RecordForm;

/**
 * The form for a loan's amount, interest, repayments and dates, used by the new loan wizard and by
 * Edit on the loan screen. Checked with the model's Loan.Details.validate(), plus checks that the
 * amounts and dates were typed correctly.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class LoanDetailsFormController implements RecordForm<Loan.Details> {

    private static final String BAD_DATE = "Enter a real date, e.g. 01/11/2026.";

    @FXML private TextField principalField;
    @FXML private ComboBox<Loan.RateType> rateTypeBox;
    @FXML private TextField rateField;
    @FXML private ComboBox<Loan.RateFrequency> rateFrequencyBox;
    @FXML private TextField paymentField;
    @FXML private ComboBox<Loan.PaymentFrequency> paymentFrequencyBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker renewalDatePicker;
    @FXML private Label principalError;
    @FXML private Label rateTypeError;
    @FXML private Label rateAmountError;
    @FXML private Label rateFrequencyError;
    @FXML private Label paymentAmountError;
    @FXML private Label paymentFrequencyError;
    @FXML private Label startDateError;
    @FXML private Label renewalDateError;

    private DateInput startDate;
    private DateInput renewalDate;
    private FieldErrors errors;

    @FXML
    private void initialize() {
        Choices.fill(rateTypeBox, ConstantCategory.RATE_TYPE, Loan.RateType.values(), Loan.RateType.UNKNOWN, LoanText::rateTypeName);
        Choices.fill(rateFrequencyBox, ConstantCategory.RATE_FREQUENCY, Loan.RateFrequency.values(), Loan.RateFrequency.UNKNOWN,
                Loan.RateFrequency::code);
        Choices.fill(paymentFrequencyBox, ConstantCategory.PAYMENT_FREQUENCY, Loan.PaymentFrequency.values(), Loan.PaymentFrequency.UNKNOWN,
                LoanText::paymentFrequencyName);
        startDate = new DateInput(startDatePicker);
        renewalDate = new DateInput(renewalDatePicker);

        errors = new FieldErrors()
                .add("principal", principalField, principalError)
                .add("rateType", rateTypeBox, rateTypeError)
                .add("rateAmount", rateField, rateAmountError)
                .add("rateFrequency", rateFrequencyBox, rateFrequencyError)
                .add("paymentAmount", paymentField, paymentAmountError)
                .add("paymentFrequency", paymentFrequencyBox, paymentFrequencyError)
                .add("startDate", startDatePicker, startDateError)
                .add("renewalDate", renewalDatePicker, renewalDateError);
        clear(true);
    }

    /** Empties the form for a new loan, with sensible starting points that can all be changed; first is ignored. */
    @Override
    public void clear(boolean first) {
        principalField.clear();
        rateTypeBox.setValue(null);
        rateField.clear();
        rateFrequencyBox.setValue(Choices.preferred(rateFrequencyBox, Loan.RateFrequency.YEARLY));
        paymentField.clear();
        paymentFrequencyBox.setValue(Choices.preferred(paymentFrequencyBox, Loan.PaymentFrequency.MONTHLY));
        startDate.set(LocalDate.now());
        renewalDate.set(null);
        errors.show(List.of());
    }

    /** True once any amount has been typed or a rate type chosen, so cancelling should ask first. */
    public boolean hasInput() {
        return !principalField.getText().isBlank() || !rateField.getText().isBlank()
                || !paymentField.getText().isBlank() || rateTypeBox.getValue() != null
                || !renewalDate.isBlank();
    }

    /** Puts an existing loan's details into the form for editing. */
    @Override
    public void fill(Loan.Details details) {
        principalField.setText(plain(details.principal()));
        rateTypeBox.setValue(details.rateType());
        rateField.setText(plain(details.rateAmount()));
        rateFrequencyBox.setValue(details.rateFrequency());
        paymentField.setText(plain(details.paymentAmount()));
        paymentFrequencyBox.setValue(details.paymentFrequency());
        startDate.set(details.startDate());
        renewalDate.set(details.renewalDate());
        errors.show(List.of());
    }

    /** Checks the details, showing any problems. Returns true when they can be saved. */
    @Override
    public boolean check() {
        List<FieldError> problems = new ArrayList<>();
        addTypingProblems(problems);
        // A field with a typing problem shows that message rather than the model's
        problems.addAll(read().validate());
        return errors.show(problems);
    }

    /** What the form holds now. Amounts that aren't numbers read as none; check() reports them. */
    @Override
    public Loan.Details read() {
        return new Loan.Details(
                Numbers.parse(principalField.getText()),
                rateTypeBox.getValue(),
                Numbers.parse(rateField.getText()),
                rateFrequencyBox.getValue(),
                startDate.value(),
                Numbers.parse(paymentField.getText()),
                paymentFrequencyBox.getValue(),
                renewalDate.value());
    }

    /** Amounts that aren't numbers and dates that aren't real; the model can't see these. */
    private void addTypingProblems(List<FieldError> problems) {
        addIfInvalid(problems, principalField, "principal", "Enter the amount as a number, e.g. 450000.");
        addIfInvalid(problems, rateField, "rateAmount", "Enter the rate as a number, e.g. 6.29.");
        addIfInvalid(problems, paymentField, "paymentAmount", "Enter the repayment as a number, e.g. 1312.14.");
        if (!startDate.isValid()) {
            startDate.showTyped();
            problems.add(new FieldError("startDate", BAD_DATE));
        }
        if (!renewalDate.isValid()) {
            renewalDate.showTyped();
            problems.add(new FieldError("renewalDate", BAD_DATE));
        }
    }

    private static String plain(BigDecimal amount) {
        return amount == null ? "" : amount.toPlainString();
    }

    private static void addIfInvalid(List<FieldError> problems, TextField field, String name, String message) {
        if (Numbers.isInvalid(field.getText())) {
            problems.add(new FieldError(name, message));
        }
    }

    @Override
    public FieldErrors fieldErrors() {
        return errors;
    }
}
