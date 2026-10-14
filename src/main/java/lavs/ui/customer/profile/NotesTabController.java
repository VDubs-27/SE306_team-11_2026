package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** The Notes tab: notes staff have written about the customer, newest first. */
public class NotesTabController {

    private static final Comparator<NoteRow> NEWEST_FIRST = Comparator.comparing(
            NoteRow::dateAdded, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder()));

    /** Widths for the Author and Added fields, so they line up from card to card like columns. */
    private static final double AUTHOR_WIDTH = 180;
    private static final double ADDED_WIDTH = 120;

    @FXML private VBox noteList;

    void show(List<NoteRow> notes) {
        RecordTiles.fill(noteList, notes.stream()
                .sorted(NEWEST_FIRST)
                .map(this::card)
                .toList(), "No notes recorded.");
    }

    @FXML
    private void handleAddNote() {
        // Later: open the add note form
    }

    private void handleEditNote(NoteRow note) {
        // Later: open the note details for this note
    }

    /** A card: labelled author, date and type along the top with Edit, then the note's text. */
    private VBox card(NoteRow note) {
        Label type = isBlank(note.type())
                ? new Label(ProfileFormat.NOT_RECORDED)
                : RecordTiles.badge(note.type(), "badge-neutral");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button edit = new Button("Edit");
        edit.getStyleClass().add("button-secondary");
        edit.setOnAction(event -> handleEditNote(note));

        HBox top = new HBox(16,
                field("Author", new Label(ProfileFormat.orNotRecorded(note.author())), AUTHOR_WIDTH),
                field("Added", new Label(ProfileFormat.date(note.dateAdded())), ADDED_WIDTH),
                field("Type", type, Region.USE_COMPUTED_SIZE),
                spacer, edit);
        top.setAlignment(Pos.CENTER_LEFT);

        Region divider = new Region();
        divider.getStyleClass().add("note-divider");

        Label text = new Label(ProfileFormat.orNotRecorded(note.text()));
        text.getStyleClass().add("note-text");
        text.setWrapText(true);

        VBox card = new VBox(top, divider, text);
        card.getStyleClass().addAll("record-tile", "note-card");
        return card;
    }

    /** A small caption with its value under it, e.g. "Author" over "Bob Ross". */
    private static VBox field(String caption, Label value, double width) {
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("tile-caption");
        if (!value.getStyleClass().contains("badge")) {
            value.getStyleClass().add("tile-value");
        }
        VBox field = new VBox(2, captionLabel, value);
        field.setAlignment(Pos.CENTER_LEFT);
        field.setPrefWidth(width);
        field.setMinWidth(Region.USE_PREF_SIZE);
        return field;
    }
}
