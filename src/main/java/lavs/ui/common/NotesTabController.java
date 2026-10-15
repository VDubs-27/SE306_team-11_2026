package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A Notes tab, on a customer's profile or a loan: notes staff have written, newest first, a page at
 * a time, which can be added, changed or removed. The screen it's on gives it the notes and hears
 * about changes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NotesTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node noteForm;
    @FXML private NoteFormController noteFormController;
    @FXML private VBox noteList;
    @FXML private PagerController pagerController;

    private EditableList<NoteRow> notes;

    @FXML
    private void initialize() {
        notes = new EditableList<>(new RecordEditor<>(noteList, "note", noteForm, noteFormController),
                noteList, NoteCards::card, "No notes recorded.")
                .paged(pagerController, PAGE_SIZE)
                .newestFirst();
    }

    /** What happens after a note is added, changed or removed, e.g. the screen updates the tab's count. */
    public void setOnChanged(Runnable onChanged) {
        notes.onChanged(onChanged);
    }

    /** Shows these notes, newest first, from the first page. */
    public void use(List<NoteRow> recorded) {
        notes.use(new ArrayList<>(recorded));
    }

    /** How many notes there are, for the tab's count. */
    public long count() {
        return notes.items().size();
    }

    @FXML
    private void handleAddNote() {
        notes.add();
    }
}
