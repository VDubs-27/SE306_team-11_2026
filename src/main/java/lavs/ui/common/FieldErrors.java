package lavs.ui.common;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;

import lavs.logic.model.FieldError;

/**
 * Shows the model's validation errors on a form: a red outline on each input with a problem and
 * its message underneath. An error clears as soon as the user changes that input.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class FieldErrors {

    private static final String ERROR_STYLE = "field-error";

    private final Map<String, Control> inputs = new LinkedHashMap<>();
    private final Map<String, Label> messages = new LinkedHashMap<>();

    /** Links a model field name (as used in FieldError) to its input and the label under it. */
    public FieldErrors add(String field, Control input, Label message) {
        inputs.put(field, input);
        messages.put(field, message);
        // Let a long message wrap onto more lines instead of being cut off in a narrow column
        message.setWrapText(true);
        message.setMinHeight(Region.USE_PREF_SIZE);
        hide(message);
        if (input instanceof TextInputControl text) {
            text.textProperty().addListener((obs, oldText, newText) -> clear(field));
        } else if (input instanceof ComboBoxBase<?> picker) {
            picker.valueProperty().addListener((obs, oldValue, newValue) -> clear(field));
        }
        if (input instanceof DatePicker datePicker) {
            datePicker.getEditor().textProperty().addListener((obs, oldText, newText) -> clear(field));
        }
        return this;
    }

    /**
     * Clears old errors and shows these, one message per field. Focuses the first input with a
     * problem. Returns true when there were no errors.
     */
    public boolean show(List<FieldError> errors) {
        inputs.keySet().forEach(this::clear);
        Set<String> shown = new HashSet<>();
        Control first = null;
        for (FieldError error : errors) {
            Control input = inputs.get(error.field());
            if (input == null || !shown.add(error.field())) {
                continue;
            }
            input.getStyleClass().add(ERROR_STYLE);
            Label message = messages.get(error.field());
            message.setText("ⓘ  " + error.message());
            message.setVisible(true);
            message.setManaged(true);
            first = first == null ? input : first;
        }
        if (first != null) {
            first.requestFocus();
        }
        return errors.isEmpty();
    }

    private void clear(String field) {
        inputs.get(field).getStyleClass().remove(ERROR_STYLE);
        hide(messages.get(field));
    }

    private static void hide(Label message) {
        message.setVisible(false);
        message.setManaged(false);
    }
}
