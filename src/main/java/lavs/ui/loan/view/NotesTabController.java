package lavs.ui.loan.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import lavs.logic.model.loan.Loan;
import lavs.ui.common.FormDialog;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteFormController;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The loan screen's Notes tab: notes staff have written about the loan, newest first, a page at
 * a time. While the loan is New, notes can be added, changed or removed; after that they're locked.
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
    @FXML private Button addButton;
    @FXML private Label lockNote;

    private final List<NoteRow> notes = new ArrayList<>();
    private Loan loan;
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

    /** Shows the notes, with Add and Edit only while the loan is New. Returns how many there are. */
    long show(Loan loan) {
        this.loan = loan;
        boolean editable = isEditable();
        addButton.setVisible(editable);
        addButton.setManaged(editable);
        lockNote.setText(editable
                ? "Notes are shared with everyone who opens this loan, newest first. Keep them factual."
                : "This loan is " + loan.status().code().toLowerCase(Locale.ROOT)
                        + ", so its notes can no longer be changed.");
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

    /** One page of notes; Edit is offered only while the loan is New. */
    private void draw(List<NoteRow> page) {
        boolean editable = isEditable();
        RecordTiles.fill(noteList, page.stream()
                .map(note -> NoteCards.card(note.author(), note.dateAdded(), note.type(), note.text(),
                        editable ? () -> handleEditNote(note) : null))
                .toList(), "No notes recorded.");
    }

    private boolean isEditable() {
        return loan.status() == Loan.Status.NEW;
    }
}
