package lavs.ui.customer.forms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import lavs.logic.model.FieldError;
import lavs.logic.model.customer.Employer;
import lavs.ui.common.Choices;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.Numbers;

/**
 * The form for one employer, used in a pop-up to add or edit. It checks itself with the model's
 * Employer.Details.validate(), plus a check that the salary is a number.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class EmployerFormController {

    private static final String BAD_SALARY = "Enter the salary as a number, e.g. 78000.";

    @FXML private ComboBox<Employer.Type> typeBox;
    @FXML private TextField nameField;
    @FXML private TextField positionField;
    @FXML private TextField salaryField;
    @FXML private Label typeError;
    @FXML private Label nameError;
    @FXML private Label positionError;
    @FXML private Label salaryError;

    private FieldErrors errors;

    @FXML
    private void initialize() {
        Choices.fill(typeBox, Employer.Type.values(), Employer.Type.UNKNOWN, Employer.Type::code);
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("name", nameField, nameError)
                .add("position", positionField, positionError)
                .add("salary", salaryField, salaryError);
    }

    /** Empties the form for a new employer. The first employer is the primary one to start with. */
    public void clear(boolean first) {
        fill(new Employer.Details(first ? Employer.Type.PRIMARY : Employer.Type.SECONDARY, "", "", null));
    }

    /** Puts an existing employer into the form for editing. */
    public void fill(Employer.Details employer) {
        typeBox.setValue(employer.type());
        nameField.setText(employer.name() == null ? "" : employer.name());
        positionField.setText(employer.position() == null ? "" : employer.position());
        salaryField.setText(employer.salary() == null ? "" : employer.salary().toPlainString());
        errors.show(List.of());
    }

    /** Checks the form, showing any problems. Returns true when it can be saved. */
    public boolean check() {
        List<FieldError> problems = new ArrayList<>(read().validate());
        if (Numbers.isInvalid(salaryField.getText()) || isNegative(salary())) {
            problems.add(new FieldError("salary", BAD_SALARY));
        }
        return errors.show(problems);
    }

    /** What the form holds now. A salary that isn't a number reads as none; check() reports it. */
    public Employer.Details read() {
        return new Employer.Details(
                typeBox.getValue(),
                nameField.getText().trim(),
                positionField.getText().trim(),
                salary());
    }

    /** The salary typed, allowing "$" and thousands commas, or null when blank or not a number. */
    private BigDecimal salary() {
        return Numbers.parse(salaryField.getText());
    }

    private static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }
}
