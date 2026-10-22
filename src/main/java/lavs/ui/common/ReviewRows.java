package lavs.ui.common;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The pieces of a review page before saving: checklist items and "name ...... value" rows.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class ReviewRows {

    private ReviewRows() {
    }

    /** A ticked item (or a grey dash when skipped) with a heading and a muted line under it. */
    public static HBox checkItem(boolean done, String heading, String detail) {
        Label tick = new Label(done ? "✓" : "–");
        tick.getStyleClass().addAll("step-circle", done ? "step-done" : "review-skipped");
        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("review-heading");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().addAll("text-muted", "text-small");
        detailLabel.setWrapText(true);
        HBox item = new HBox(12, tick, new VBox(2, headingLabel, detailLabel));
        item.setAlignment(Pos.TOP_LEFT);
        return item;
    }

    /** "Date of birth ........ 09/01/1978", with a line under it. */
    public static HBox summaryRow(String name, String value) {
        Label nameText = new Label(name);
        nameText.getStyleClass().add("text-muted");
        Label valueText = new Label(value);
        valueText.getStyleClass().add("review-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(nameText, spacer, valueText);
        row.getStyleClass().add("review-row");
        return row;
    }

    /** e.g. "1 phone", "2 emails". */
    public static String count(int howMany, String thing) {
        return howMany + " " + thing + (howMany == 1 ? "" : "s");
    }
}
