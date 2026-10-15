package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.FormDialog;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteFormController;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Notes tab: notes staff have written about the customer, a page at a time. Add and Edit use
 * the same note pop-up as the loan screen.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NotesTabController {

    @FXML private VBox noteList;
    @FXML private PagerController pagerController;
    @FXML private Node noteForm;
    @FXML private NoteFormController noteFormController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<NoteRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(noteList, items.stream()
                .map(note -> NoteCards.card(note.author(), note.dateAdded(), note.type(), note.text(),
                        () -> handleEditNote(note)))
                .toList(), "No notes recorded."));
    }

    @FXML
    private void handleAddNote() {
        noteFormController.clear();
        if (FormDialog.show(noteList, "Add note", noteForm, noteFormController::check, false)
                == FormDialog.Result.SAVED) {
            // Later: CustomerService.addNote(customerNumber, type, text), then reload the tab
        }
    }

    private void handleEditNote(NoteRow note) {
        noteFormController.fill(note.type(), note.text());
        switch (FormDialog.show(noteList, "Edit note", noteForm, noteFormController::check, true)) {
            case SAVED -> {
                // Later: CustomerService.updateNote(note.id(), type, text), then reload the tab
            }
            case REMOVED -> {
                // Later: CustomerService.removeNote(note.id()), then reload the tab
            }
            default -> {
                // Cancelled: nothing to do
            }
        }
    }
}
