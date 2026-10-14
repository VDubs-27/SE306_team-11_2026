package lavs.ui.customer.profile;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Builds the bordered rows used to list addresses, contacts and employers. */
final class RecordTiles {

    private RecordTiles() {
    }

    /** A row: type on the left, two lines of detail, then any badges and an Edit button. */
    static HBox tile(String type, String line1, String line2, List<Label> badges, Runnable onEdit) {
        Label typeLabel = styled(new Label(type), "tile-type");
        VBox lines = new VBox(2, styled(new Label(line1), "tile-line1"));
        lines.setAlignment(Pos.CENTER_LEFT);
        if (line2 != null && !line2.isBlank()) {
            lines.getChildren().add(styled(new Label(line2), "tile-line2"));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(Pos.CENTER_RIGHT);
        badgeRow.getChildren().setAll(badges);

        Button edit = styled(new Button("Edit"), "button-secondary");
        edit.setOnAction(event -> onEdit.run());

        HBox tile = new HBox(typeLabel, lines, spacer, badgeRow, edit);
        tile.getStyleClass().add("record-tile");
        return tile;
    }

    /** Shows the tiles in a list, or a short message when there are none. */
    static void fill(VBox list, List<HBox> tiles, String emptyMessage) {
        if (tiles.isEmpty()) {
            list.getChildren().setAll(styled(new Label(emptyMessage), "text-muted"));
        } else {
            list.getChildren().setAll(tiles);
        }
    }

    /** The badge marking a customer's main address, phone or email. */
    static Label primaryBadge() {
        return badge("✓ Primary", "badge-primary");
    }

    /** A small pill such as "Primary" or "Mailing". */
    static Label badge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }

    private static <T extends Node> T styled(T node, String styleClass) {
        node.getStyleClass().add(styleClass);
        return node;
    }
}
