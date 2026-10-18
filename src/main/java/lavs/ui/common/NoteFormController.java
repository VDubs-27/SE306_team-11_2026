package lavs.ui.common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import lavs.logic.model.FieldError;
import lavs.logic.model.note.Note;

/**
 * The form for one note, used in a pop-up to add or edit: what kind of note it is and what it says.
 * The mainframe records who wrote a note and when, so the form only has its type and text.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NoteFormController implements RecordForm<NoteRow> {

    /** The kinds of note LAVS keeps; the mainframe sends no list of them, so they come from Note.Type. */
    private static final List<String> TYPES = Arrays.stream(Note.Type.values())
            .filter(type -> type != Note.Type.UNKNOWN)
            .map(Note.Type::code)
            .toList();

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
                ? new NoteRow(0, typeBox.getValue(), null, LocalDate.now(), text())
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
