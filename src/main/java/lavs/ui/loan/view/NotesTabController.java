package lavs.ui.loan.view;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.ui.common.EditableList;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteFormController;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;

/**
 * The loan screen's Notes tab: notes staff have written about the loan, a page at a time. Notes
 * can be added, changed or removed whatever the loan's status.
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
        // Later: save through LoanService (add, update and delete note); until then changes only show on this screen
        notes = new EditableList<>(new RecordEditor<>(noteList, "note", noteForm, noteFormController),
                noteList, NoteCards::card, "No notes recorded.")
                .paged(pagerController, PAGE_SIZE)
                .newestFirst();
    }

    /** What happens after a note is added, changed or removed: the loan screen updates the tab's count. */
    void setOnChanged(Runnable onChanged) {
        notes.onChanged(onChanged);
    }

    /** Shows the loan's notes, newest first, from the first page. */
    void use(List<NoteRow> loanNotes) {
        notes.use(new ArrayList<>(loanNotes));
    }

    /** How many notes there are, for the tab's count. */
    long count() {
        return notes.items().size();
    }

    @FXML
    private void handleAddNote() {
        notes.add();
    }
}
