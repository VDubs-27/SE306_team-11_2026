package lavs.ui.customer.forms;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import lavs.logic.model.customer.Address;
import lavs.ui.common.Choices;
import lavs.ui.common.FieldErrors;
import lavs.ui.common.RecordForm;

/**
 * The form for one address, used in a pop-up to add or edit. It checks itself with the model's
 * Address.Details.validate(), so the rules live in one place.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class AddressFormController implements RecordForm<Address.Details> {

    private static final String HOME_COUNTRY = "New Zealand";

    @FXML private ComboBox<Address.Type> typeBox;
    @FXML private VBox otherTypeFieldBox;
    @FXML private TextField otherTypeField;
    @FXML private TextField line1Field;
    @FXML private TextField line2Field;
    @FXML private TextField suburbField;
    @FXML private TextField cityField;
    @FXML private TextField postCodeField;
    @FXML private TextField countryField;
    @FXML private CheckBox primaryBox;
    @FXML private CheckBox mailingBox;
    @FXML private Label typeError;
    @FXML private Label otherTypeError;
    @FXML private Label line1Error;
    @FXML private Label line2Error;
    @FXML private Label suburbError;
    @FXML private Label cityError;
    @FXML private Label postCodeError;
    @FXML private Label countryError;

    private FieldErrors errors;

    @FXML
    private void initialize() {
        Choices.fill(typeBox, Address.Type.values(), Address.Type.UNKNOWN, Address.Type::code);
        // "Describe the address" only applies to an Other address
        typeBox.valueProperty().addListener((obs, oldType, type) -> showOtherType(type == Address.Type.OTHER));
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("otherType", otherTypeField, otherTypeError)
                .add("line1", line1Field, line1Error)
                .add("line2", line2Field, line2Error)
                .add("suburb", suburbField, suburbError)
                .add("city", cityField, cityError)
                .add("postCode", postCodeField, postCodeError)
                .add("country", countryField, countryError);
    }

    /** Empties the form for a new address. The first address is primary and mailing to start with. */
    @Override
    public void clear(boolean firstAddress) {
        fill(new Address.Details(null, "", "", "", "", "", "", HOME_COUNTRY, firstAddress, firstAddress));
    }

    /** Puts an existing address into the form for editing. */
    @Override
    public void fill(Address.Details address) {
        typeBox.setValue(address.type());
        otherTypeField.setText(text(address.otherType()));
        line1Field.setText(text(address.line1()));
        line2Field.setText(text(address.line2()));
        suburbField.setText(text(address.suburb()));
        cityField.setText(text(address.city()));
        postCodeField.setText(text(address.postCode()));
        countryField.setText(text(address.country()));
        primaryBox.setSelected(address.isPrimary());
        mailingBox.setSelected(address.isMailing());
        showOtherType(address.type() == Address.Type.OTHER);
        errors.show(List.of());
    }

    /** Checks the form, showing any problems. Returns true when it can be saved. */
    @Override
    public boolean check() {
        return errors.show(read().validate());
    }

    /** What the form holds now. */
    @Override
    public Address.Details read() {
        Address.Type type = typeBox.getValue();
        return new Address.Details(
                type,
                type == Address.Type.OTHER ? otherTypeField.getText().trim() : null,
                line1Field.getText().trim(),
                line2Field.getText().trim(),
                suburbField.getText().trim(),
                cityField.getText().trim(),
                postCodeField.getText().trim(),
                countryField.getText().trim(),
                primaryBox.isSelected(),
                mailingBox.isSelected());
    }

    private void showOtherType(boolean show) {
        otherTypeFieldBox.setVisible(show);
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }

    @Override
    public FieldErrors fieldErrors() {
        return errors;
    }
}
