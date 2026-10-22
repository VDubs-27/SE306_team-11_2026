package lavs.ui.common;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A Notes tab, on a customer's profile or a loan: notes staff have written, a page at a time from the
 * mainframe, which can be added, changed or removed there. The mainframe records who wrote each note.
 * AI-assisted: written by Jack, refined with Claude.
 */
public class NotesTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node noteForm;
    @FXML private NoteFormController noteFormController;
    @FXML private VBox noteList;
    @FXML private PagerController pagerController;

    private ServiceList<NoteRow, NoteRow> notes;

    @FXML
    private void initialize() {
        notes = new ServiceList<>(new RecordEditor<>(noteList, "note", noteForm, noteFormController),
                noteList, pagerController, PAGE_SIZE, NoteCards::card, note -> note, "No notes recorded.");
    }

    /** Shows the notes kept on the mainframe, e.g. a customer's, and saves changes there; onTotal hears the count. */
    public void show(ServiceList.Source<NoteRow, NoteRow> source, LongConsumer onTotal) {
        notes.onTotal(onTotal).show(source);
    }

    @FXML
    private void handleAddNote() {
        notes.add();
    }
}
