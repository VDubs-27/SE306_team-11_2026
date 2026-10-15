package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteFormController;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordEditor;
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

    private RecordEditor<NoteRow> editor;

    @FXML
    private void initialize() {
        editor = new RecordEditor<>(noteList, "note", noteForm, noteFormController);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<NoteRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(noteList, items.stream()
                .map(note -> NoteCards.card(note, () -> handleEditNote(note)))
                .toList(), "No notes recorded."));
    }

    @FXML
    private void handleAddNote() {
        editor.add(false, note -> {
            // Later: CustomerService.addNote(customerNumber, note), then reload the tab
        });
    }

    private void handleEditNote(NoteRow note) {
        editor.edit(note, changed -> {
            // Later: CustomerService.updateNote(note.id(), changed), then reload the tab
        }, () -> {
            // Later: CustomerService.removeNote(note.id()), then reload the tab
        });
    }
}
