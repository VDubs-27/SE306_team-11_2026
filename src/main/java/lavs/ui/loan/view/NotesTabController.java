package lavs.ui.loan.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.ui.common.FormDialog;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteFormController;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The loan screen's Notes tab: notes staff have written about the loan, newest first, a page at
 * a time. Notes can be added, changed or removed whatever the loan's status.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NotesTabController {

    private static final long PAGE_SIZE = 5;
    /** Who new notes are by. */
    // Later: the signed-in user, once there is a login
    private static final String CURRENT_USER = "Bob Ross";

    @FXML private Node noteForm;
    @FXML private NoteFormController noteFormController;
    @FXML private VBox noteList;
    @FXML private PagerController pagerController;

    private final List<NoteRow> notes = new ArrayList<>();
    private Runnable onChanged = () -> { };

    /** What happens after a note is added, changed or removed: the loan screen redraws this tab and its count. */
    void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    /** The loan's notes when the screen opens, newest first. */
    void use(List<NoteRow> notes) {
        this.notes.clear();
        this.notes.addAll(notes);
    }

    /** Shows the notes and returns how many there are, for the tab's count. */
    long show() {
        return pagerController.show(page -> SampleLoans.pageOf(List.copyOf(notes), page, PAGE_SIZE), this::draw);
    }

    @FXML
    private void handleAddNote() {
        noteFormController.clear();
        if (FormDialog.show(noteList, "Add note", noteForm, noteFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: LoanService.addNote(loan, ...); until then the change only shows on this screen
            notes.add(0, new NoteRow(0, noteFormController.type(), CURRENT_USER, LocalDate.now(),
                    noteFormController.text()));
            onChanged.run();
        }
    }

    private void handleEditNote(NoteRow note) {
        noteFormController.fill(note.type(), note.text());
        FormDialog.Result result = FormDialog.show(noteList, "Edit note", noteForm, noteFormController::check, true);
        // Later: LoanService.updateNote / deleteNote; until then the change only shows on this screen
        if (result == FormDialog.Result.SAVED) {
            notes.set(notes.indexOf(note), new NoteRow(note.id(), noteFormController.type(), note.author(),
                    note.dateAdded(), noteFormController.text()));
            onChanged.run();
        } else if (result == FormDialog.Result.REMOVED) {
            notes.remove(note);
            onChanged.run();
        }
    }

    private void draw(List<NoteRow> page) {
        RecordTiles.fill(noteList, page.stream()
                .map(note -> NoteCards.card(note.author(), note.dateAdded(), note.type(), note.text(),
                        () -> handleEditNote(note)))
                .toList(), "No notes recorded.");
    }
}
