package lavs.ui.customer.forms;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import lavs.logic.model.FieldError;
import lavs.logic.model.customer.Customer;
import lavs.ui.common.DateInput;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.RecordForm;

/**
 * The form for a customer's personal details: title, names, date of birth and nationality. Used
 * as the first step of the new customer wizard, and in a pop-up to edit them from the profile.
 * Checked with the model's Customer.Details.validate(), plus a check that the date is a real one.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class CustomerDetailsFormController implements RecordForm<Customer.Details> {

    private static final String BAD_DATE = "Enter a real date, e.g. 09/01/1978.";

    @FXML private ComboBox<String> titleBox;
    @FXML private TextField firstNameField;
    @FXML private TextField familyNameField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> nationalityBox;
    @FXML private Label titleError;
    @FXML private Label firstNameError;
    @FXML private Label familyNameError;
    @FXML private Label dateOfBirthError;
    @FXML private Label nationalityError;

    private DateInput dateOfBirth;
    private FieldErrors errors;

    @FXML
    private void initialize() {
        titleBox.getItems().setAll(CustomerChoices.TITLES);
        nationalityBox.getItems().setAll(CustomerChoices.NATIONALITIES);
        dateOfBirth = new DateInput(dateOfBirthPicker);
        errors = new FieldErrors()
                .add("title", titleBox, titleError)
                .add("firstName", firstNameField, firstNameError)
                .add("familyName", familyNameField, familyNameError)
                .add("dateOfBirth", dateOfBirthPicker, dateOfBirthError)
                .add("nationality", nationalityBox, nationalityError);
    }

    /** True once anything has been entered, so cancelling should ask first. */
    public boolean hasInput() {
        return titleBox.getValue() != null || !firstNameField.getText().isBlank()
                || !familyNameField.getText().isBlank() || !dateOfBirth.isBlank()
                || nationalityBox.getValue() != null;
    }

    /** Empties the form for a new customer; first is ignored. */
    @Override
    public void clear(boolean first) {
        fill(new Customer.Details(null, "", "", null, null));
    }

    /** Puts a customer's details into the form for editing. */
    @Override
    public void fill(Customer.Details details) {
        titleBox.setValue(details.title());
        firstNameField.setText(details.firstName());
        familyNameField.setText(details.familyName());
        dateOfBirthPicker.setValue(details.dateOfBirth());
        nationalityBox.setValue(details.nationality());
        errors.show(List.of());
    }

    /** Checks the details, showing any problems. Returns true when they can be saved. */
    @Override
    public boolean check() {
        List<FieldError> problems = new ArrayList<>();
        if (!dateOfBirth.isValid()) {
            dateOfBirth.showTyped();
            problems.add(new FieldError("dateOfBirth", BAD_DATE));
        }
        problems.addAll(read().validate(LocalDate.now()));
        return errors.show(problems);
    }

    /** What the form holds now, with spaces trimmed from the names. */
    @Override
    public Customer.Details read() {
        return new Customer.Details(
                titleBox.getValue(),
                firstNameField.getText().trim(),
                familyNameField.getText().trim(),
                dateOfBirth.value(),
                nationalityBox.getValue());
    }
}
