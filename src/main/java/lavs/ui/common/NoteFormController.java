package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import lavs.logic.model.FieldError;

/**
 * The form for one note, used in a pop-up to add or edit: what kind of note it is and what it says.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NoteFormController {

    /** Temporary choices; replaced by the mainframe's note types once there is a service to read them. */
    private static final List<String> TYPES = List.of("General", "Application", "Contact", "Identification", "Loan");

    @FXML private ComboBox<String> typeBox;
    @FXML private TextArea textArea;
    @FXML private Label typeError;
    @FXML private Label textError;

    private FieldErrors errors;

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(TYPES);
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("text", textArea, textError);
    }

    /** Empties the form for a new note. */
    public void clear() {
        fill(null, null);
    }

    /** Puts an existing note into the form for editing. */
    public void fill(String type, String text) {
        typeBox.setValue(type);
        textArea.setText(text == null ? "" : text);
        errors.show(List.of());
    }

    /** Checks there is a type and some text. Returns true when it can be saved. */
    public boolean check() {
        List<FieldError> problems = new ArrayList<>();
        if (typeBox.getValue() == null) {
            problems.add(new FieldError("type", "Note type is required."));
        }
        if (text().isEmpty()) {
            problems.add(new FieldError("text", "Write the note."));
        }
        return errors.show(problems);
    }

    public String type() {
        return typeBox.getValue();
    }

    /** The note's text, without spaces or blank lines at either end. */
    public String text() {
        return textArea.getText() == null ? "" : textArea.getText().strip();
    }
}
