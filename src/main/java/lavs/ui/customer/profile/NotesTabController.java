package lavs.ui.customer.profile;

import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import lavs.logic.model.Page;
import lavs.ui.common.NoteCards;
import lavs.ui.common.NoteRow;
import lavs.ui.common.PagerController;
import lavs.ui.common.RecordTiles;

/**
 * The Notes tab: notes staff have written about the customer, a page at a time.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class NotesTabController {

    @FXML private VBox noteList;
    @FXML private PagerController pagerController;

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<NoteRow>> loadPage) {
        return pagerController.show(loadPage, items -> RecordTiles.fill(noteList, items.stream()
                .map(note -> NoteCards.card(note.author(), note.dateAdded(), note.type(), note.text(),
                        () -> handleEditNote(note)))
                .toList(), "No notes recorded."));
    }

    @FXML
    private void handleAddNote() {
        // Later: open the add note form
    }

    private void handleEditNote(NoteRow note) {
        // Later: open the note details for this note
    }
}
