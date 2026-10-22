package lavs.ui.customer.forms;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.customer.Email;
import lavs.ui.common.Choices;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.Nodes;
import lavs.ui.common.RecordForm;

/**
 * The form for one email address, used in a pop-up to add or edit. It checks itself with the
 * model's Email.Details.validate().
 * AI-assisted: written by Jack, refined with Claude.
 */
public class EmailFormController implements RecordForm<Email.Details> {

    @FXML private ComboBox<Email.Type> typeBox;
    @FXML private VBox otherTypeFieldBox;
    @FXML private TextField otherTypeField;
    @FXML private TextField addressField;
    @FXML private CheckBox primaryBox;
    @FXML private Label typeError;
    @FXML private Label otherTypeError;
    @FXML private Label addressError;

    private FieldErrors errors;

    @FXML
    private void initialize() {
        Choices.fill(typeBox, ConstantCategory.EMAIL_TYPE, Email.Type.values(), Email.Type.UNKNOWN, Email.Type::code);
        // "Describe the email" only applies to an Other email
        typeBox.valueProperty().addListener((obs, oldType, type) -> showOtherType(type == Email.Type.OTHER));
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("otherType", otherTypeField, otherTypeError)
                .add("address", addressField, addressError);
    }

    /** Empties the form for a new email. The first email is primary to start with. */
    @Override
    public void clear(boolean first) {
        fill(new Email.Details(null, "", "", first));
    }

    /** Puts an existing email into the form for editing. */
    @Override
    public void fill(Email.Details email) {
        typeBox.setValue(email.type());
        otherTypeField.setText(email.otherType() == null ? "" : email.otherType());
        addressField.setText(email.address() == null ? "" : email.address());
        primaryBox.setSelected(email.isPrimary());
        showOtherType(email.type() == Email.Type.OTHER);
        errors.show(List.of());
    }

    /** Checks the form, showing any problems. Returns true when it can be saved. */
    @Override
    public boolean check() {
        return errors.show(read().validate());
    }

    /** What the form holds now. */
    @Override
    public Email.Details read() {
        Email.Type type = typeBox.getValue();
        return new Email.Details(
                type,
                type == Email.Type.OTHER ? otherTypeField.getText().trim() : null,
                addressField.getText().trim(),
                primaryBox.isSelected());
    }

    private void showOtherType(boolean show) {
        Nodes.show(otherTypeFieldBox, show);
    }

    @Override
    public FieldErrors fieldErrors() {
        return errors;
    }
}
