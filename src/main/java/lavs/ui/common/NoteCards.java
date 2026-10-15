package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.time.LocalDate;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * How a note is shown, for a customer or a loan: a card with the author, date and type labelled
 * along the top, an Edit button, then the note's text cut to three lines.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class NoteCards {

    /** Widths for the Author and Added fields, so they line up from card to card like columns. */
    private static final double AUTHOR_WIDTH = 180;
    private static final double ADDED_WIDTH = 120;
    /** Longer notes are cut off with "..."; the full text opens from Edit. */
    private static final int MAX_TEXT_LINES = 3;
    private static final double LINE_SLACK = 0.3;

    private NoteCards() {
    }

    /** A card: labelled author, date and type along the top with Edit, then the note's text. */
    public static VBox card(String author, LocalDate dateAdded, String type, String text, Runnable onEdit) {
        Label typeLabel = isBlank(type)
                ? new Label(DisplayFormat.NOT_RECORDED)
                : RecordTiles.badge(type, "badge-neutral");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button edit = new Button("Edit");
        edit.getStyleClass().add("button-secondary");
        edit.setOnAction(event -> onEdit.run());

        HBox top = new HBox(16,
                field("Author", new Label(DisplayFormat.orNotRecorded(author)), AUTHOR_WIDTH),
                field("Added", new Label(DisplayFormat.date(dateAdded)), ADDED_WIDTH),
                field("Type", typeLabel, Region.USE_COMPUTED_SIZE),
                spacer, edit);
        top.setAlignment(Pos.CENTER_LEFT);

        Region divider = new Region();
        divider.getStyleClass().add("note-divider");

        Label textLabel = new Label(DisplayFormat.orNotRecorded(text));
        textLabel.getStyleClass().add("note-text");
        textLabel.setWrapText(true);
        limitLines(textLabel);

        VBox card = new VBox(top, divider, textLabel);
        card.getStyleClass().addAll("record-tile", "note-card");
        return card;
    }

    /**
     * Wraps the text onto at most three lines, ending in "..." when there's more. The TabPane sizes
     * its tabs without knowing their width, which would leave wrapped text squashed to one line, so
     * the label's minimum height is set from its real wrapped height at the width it was given.
     */
    private static void limitLines(Label label) {
        label.setAlignment(Pos.TOP_LEFT);
        Text measure = new Text();
        label.minHeightProperty().bind(Bindings.createDoubleBinding(() -> {
            measure.setFont(label.getFont());
            measure.setWrappingWidth(0);
            measure.setText("X");
            double lineHeight = measure.getLayoutBounds().getHeight();
            measure.setText("X" + "\nX".repeat(MAX_TEXT_LINES - 1));
            double maxHeight = measure.getLayoutBounds().getHeight();
            measure.setText(label.getText());
            measure.setWrappingWidth(label.getWidth());
            // A Label spaces its lines slightly wider than Text, so allow a little extra
            return Math.min(measure.getLayoutBounds().getHeight(), maxHeight) + lineHeight * LINE_SLACK;
        }, label.widthProperty(), label.fontProperty(), label.textProperty()));
        label.maxHeightProperty().bind(label.minHeightProperty());
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
