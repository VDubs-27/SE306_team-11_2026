package lavs.ui.customer.wizard;

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

/**
 * Step 1 of the new customer wizard: title, names, date of birth and nationality.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DetailsStepController {

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
        // Later: fill these from the mainframe's category lists
        titleBox.getItems().setAll(SampleCategories.TITLES);
        nationalityBox.getItems().setAll(SampleCategories.NATIONALITIES);
        dateOfBirth = new DateInput(dateOfBirthPicker);
        errors = new FieldErrors()
                .add("title", titleBox, titleError)
                .add("firstName", firstNameField, firstNameError)
                .add("familyName", familyNameField, familyNameError)
                .add("dateOfBirth", dateOfBirthPicker, dateOfBirthError)
                .add("nationality", nationalityBox, nationalityError);
    }

    /** True once anything has been entered, so cancelling should ask first. */
    boolean hasInput() {
        return titleBox.getValue() != null || !firstNameField.getText().isBlank()
                || !familyNameField.getText().isBlank() || !dateOfBirth.isBlank()
                || nationalityBox.getValue() != null;
    }

    /** Checks the details, showing any problems; when they pass, saves them in the draft. */
    boolean check(NewCustomerDraft draft) {
        List<FieldError> problems = new ArrayList<>();
        if (!dateOfBirth.isValid()) {
            dateOfBirth.showTyped();
            problems.add(new FieldError("dateOfBirth", BAD_DATE));
        }
        Customer.Details details = new Customer.Details(
                titleBox.getValue(),
                firstNameField.getText().trim(),
                familyNameField.getText().trim(),
                dateOfBirth.value(),
                nationalityBox.getValue());
        problems.addAll(details.validate(LocalDate.now()));
        if (!errors.show(problems)) {
            return false;
        }
        draft.setDetails(details);
        return true;
    }
}
