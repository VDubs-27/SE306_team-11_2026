package lavs.ui.customer.forms;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Phone;
import lavs.ui.common.Choices;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.Nodes;
import lavs.ui.common.RecordForm;

/**
 * The form for one phone number, used in a pop-up to add or edit. It checks itself with the
 * model's Phone.Details.validate().
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class PhoneFormController implements RecordForm<Phone.Details> {

    private static final String NZ_CODE = "64";

    @FXML private ComboBox<Phone.Type> typeBox;
    @FXML private VBox otherTypeFieldBox;
    @FXML private TextField otherTypeField;
    @FXML private TextField countryCodeField;
    @FXML private TextField numberField;
    @FXML private CheckBox primaryBox;
    @FXML private CheckBox canTextBox;
    @FXML private Label typeError;
    @FXML private Label otherTypeError;
    @FXML private Label countryCodeError;
    @FXML private Label numberError;

    private FieldErrors errors;

    @FXML
    private void initialize() {
        Choices.fill(typeBox, Phone.Type.values(), Phone.Type.UNKNOWN, Phone.Type::code);
        // "Describe the phone" only applies to an Other phone
        typeBox.valueProperty().addListener((obs, oldType, type) -> showOtherType(type == Phone.Type.OTHER));
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("otherType", otherTypeField, otherTypeError)
                .add("countryCode", countryCodeField, countryCodeError)
                .add("number", numberField, numberError);
    }

    /** Empties the form for a new number. The first number is primary to start with. */
    @Override
    public void clear(boolean first) {
        fill(new Phone.Details(null, "", NZ_CODE, "", first, false));
    }

    /** Puts an existing number into the form for editing. */
    @Override
    public void fill(Phone.Details phone) {
        typeBox.setValue(phone.type());
        otherTypeField.setText(text(phone.otherType()));
        countryCodeField.setText(text(phone.countryCode()));
        numberField.setText(text(phone.number()));
        primaryBox.setSelected(phone.isPrimary());
        canTextBox.setSelected(phone.canTxt());
        showOtherType(phone.type() == Phone.Type.OTHER);
        errors.show(List.of());
    }

    /** Checks the form, showing any problems. Returns true when it can be saved. */
    @Override
    public boolean check() {
        return errors.show(read().validate());
    }

    /** What the form holds now. A leading "+" on the country code is dropped. */
    @Override
    public Phone.Details read() {
        Phone.Type type = typeBox.getValue();
        return new Phone.Details(
                type,
                type == Phone.Type.OTHER ? otherTypeField.getText().trim() : null,
                countryCodeField.getText().trim().replaceFirst("^\\+", ""),
                numberField.getText().trim(),
                primaryBox.isSelected(),
                canTextBox.isSelected());
    }

    private void showOtherType(boolean show) {
        Nodes.show(otherTypeFieldBox, show);
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
