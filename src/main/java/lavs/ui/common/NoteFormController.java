package lavs.ui.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import lavs.logic.model.FieldError;

/**
 * The form for one note, used in a pop-up to add or edit: what kind of note it is and what it says.
 * A new note is by the signed-in user and dated today; an edited one keeps its author and date.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NoteFormController implements RecordForm<NoteRow> {

    /** Temporary choices; replaced by the mainframe's note types once there is a service to read them. */
    private static final List<String> TYPES = List.of("General", "Application", "Contact", "Identification", "Loan");
    /** Who new notes are by. */
    // Later: the signed-in user, once there is a login
    private static final String CURRENT_USER = "Bob Ross";

    @FXML private ComboBox<String> typeBox;
    @FXML private TextArea textArea;
    @FXML private Label typeError;
    @FXML private Label textError;

    private FieldErrors errors;
    private NoteRow editing;

    @FXML
    private void initialize() {
        typeBox.getItems().setAll(TYPES);
        errors = new FieldErrors()
                .add("type", typeBox, typeError)
                .add("text", textArea, textError);
    }

    /** Empties the form for a new note; first is ignored, as notes have no primary. */
    @Override
    public void clear(boolean first) {
        editing = null;
        show(null, null);
    }

    @Override
    public void fill(NoteRow note) {
        editing = note;
        show(note.type(), note.text());
    }

    /** Checks there is a type and some text. Returns true when it can be saved. */
    @Override
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

    @Override
    public NoteRow read() {
        return editing == null
                ? new NoteRow(0, typeBox.getValue(), CURRENT_USER, LocalDate.now(), text())
                : new NoteRow(editing.id(), typeBox.getValue(), editing.author(), editing.dateAdded(), text());
    }

    private void show(String type, String text) {
        typeBox.setValue(type);
        textArea.setText(text == null ? "" : text);
        errors.show(List.of());
    }

    /** The note's text, without spaces or blank lines at either end. */
    private String text() {
        return textArea.getText() == null ? "" : textArea.getText().strip();
    }

    @Override
    public FieldErrors fieldErrors() {
        return errors;
    }
}
