package lavs.ui.loan.wizard;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import lavs.logic.model.FieldError;
import lavs.logic.model.loan.Loan;
import lavs.ui.common.Choices;
import lavs.ui.common.DateInput;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.LoanText;
import lavs.ui.common.Numbers;

/**
 * Step 1 of the new loan wizard: the amount, interest, repayments and dates. Checked with the
 * model's Loan.Details.validate(), plus checks that the amounts and dates were typed correctly.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class LoanDetailsStepController {

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
        Choices.fill(rateTypeBox, Loan.RateType.values(), Loan.RateType.UNKNOWN, LoanText::rateTypeName);
        Choices.fill(rateFrequencyBox, Loan.RateFrequency.values(), Loan.RateFrequency.UNKNOWN,
                Loan.RateFrequency::code);
        Choices.fill(paymentFrequencyBox, Loan.PaymentFrequency.values(), Loan.PaymentFrequency.UNKNOWN,
                LoanText::paymentFrequencyName);
        startDate = new DateInput(startDatePicker);
        renewalDate = new DateInput(renewalDatePicker);

        // Sensible starting points; all can be changed
        rateFrequencyBox.setValue(Loan.RateFrequency.YEARLY);
        paymentFrequencyBox.setValue(Loan.PaymentFrequency.MONTHLY);
        startDatePicker.setValue(LocalDate.now());

        errors = new FieldErrors()
                .add("principal", principalField, principalError)
                .add("rateType", rateTypeBox, rateTypeError)
                .add("rateAmount", rateField, rateAmountError)
                .add("rateFrequency", rateFrequencyBox, rateFrequencyError)
                .add("paymentAmount", paymentField, paymentAmountError)
                .add("paymentFrequency", paymentFrequencyBox, paymentFrequencyError)
                .add("startDate", startDatePicker, startDateError)
                .add("renewalDate", renewalDatePicker, renewalDateError);
    }

    /** True once any amount has been typed or a rate type chosen, so cancelling should ask first. */
    boolean hasInput() {
        return !principalField.getText().isBlank() || !rateField.getText().isBlank()
                || !paymentField.getText().isBlank() || rateTypeBox.getValue() != null
                || !renewalDate.isBlank();
    }

    /** Checks the details, showing any problems; when they pass, saves them in the draft. */
    boolean check(NewLoanDraft draft) {
        List<FieldError> problems = new ArrayList<>();
        addTypingProblems(problems);
        Loan.Details details = new Loan.Details(
                Numbers.parse(principalField.getText()),
                rateTypeBox.getValue(),
                Numbers.parse(rateField.getText()),
                rateFrequencyBox.getValue(),
                startDate.value(),
                Numbers.parse(paymentField.getText()),
                paymentFrequencyBox.getValue(),
                renewalDate.value());
        // A field with a typing problem shows that message rather than the model's
        problems.addAll(details.validate());
        if (!errors.show(problems)) {
            return false;
        }
        draft.setDetails(details);
        return true;
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

    private static void addIfInvalid(List<FieldError> problems, TextField field, String name, String message) {
        if (Numbers.isInvalid(field.getText())) {
            problems.add(new FieldError(name, message));
        }
    }

}
