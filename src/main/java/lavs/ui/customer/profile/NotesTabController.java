package lavs.ui.customer.profile;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** The Notes tab: notes staff have written about the customer, newest first. */
public class NotesTabController {

    private static final Comparator<NoteRow> NEWEST_FIRST = Comparator.comparing(
            NoteRow::dateAdded, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()));

    @FXML private TableView<NoteRow> notesTable;
    @FXML private TableColumn<NoteRow, String> typeColumn;
    @FXML private TableColumn<NoteRow, String> authorColumn;
    @FXML private TableColumn<NoteRow, String> dateAddedColumn;
    @FXML private TableColumn<NoteRow, Void> arrowColumn;
    @FXML private Label emptyState;

    @FXML
    private void initialize() {
        typeColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.orNotRecorded(cell.getValue().type())));
        authorColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.orNotRecorded(cell.getValue().author())));
        dateAddedColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(ProfileFormat.date(cell.getValue().dateAdded())));

        ProfileTables.makeRowsOpenable(notesTable, arrowColumn, this::handleOpenNote);
    }

    void show(List<NoteRow> notes) {
        ProfileTables.show(notesTable, notesTable, emptyState, notes.stream().sorted(NEWEST_FIRST).toList());
    }

    @FXML
    private void handleAddNote() {
        // Later: open the add note form
    }

    private void handleOpenNote(NoteRow note) {
        // Later: open the note details for this note
    }
}
