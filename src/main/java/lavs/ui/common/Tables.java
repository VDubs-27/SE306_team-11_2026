package lavs.ui.common;

import java.util.List;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Set-up for tables inside tabs, such as a profile's Loans or a loan's Schedule.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Tables {

    /** Row height from .table-row-cell in app.css. */
    private static final double ROW_HEIGHT = 52;
    /** Corner radius of .table-box in app.css. */
    private static final double BOX_RADIUS = 12;

    private Tables() {
    }

    /** Draws the › in the last column and calls onOpen when a row is clicked. */
    public static <T> void makeRowsOpenable(TableView<T> table, TableColumn<T, Void> arrowColumn, Consumer<T> onOpen) {
        arrowColumn.setCellFactory(column -> new TableCell<>() {
            private final Label chevron = new Label("›");

            {
                chevron.getStyleClass().add("row-chevron");
                setAlignment(Pos.CENTER_RIGHT);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : chevron);
            }
        });

        table.setRowFactory(view -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    onOpen.accept(row.getItem());
                }
            });
            return row;
        });
        fitToRows(table);
    }

    /**
     * Shows the rows in the table. When there are none, hides the container
     * (the table, or the box around it) and shows the empty message instead.
     */
    public static <T> void show(TableView<T> table, Node container, Label emptyState, List<T> rows) {
        table.getItems().setAll(rows);

        boolean hasRows = !rows.isEmpty();
        Nodes.show(container, hasRows);
        Nodes.show(emptyState, !hasRows);
    }

    /**
     * Rounds a table's corners to fit just inside the 1px border of the box around it, so its
     * square header and rows don't paint over the box's rounded corners.
     */
    public static void fitInsideBox(TableView<?> table) {
        double radius = BOX_RADIUS - 1;
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(table.widthProperty());
        clip.heightProperty().bind(table.heightProperty());
        table.setClip(clip);
    }

    /**
     * Keeps the table just tall enough for its rows, so short lists don't leave a block of
     * empty table. Uses the header's real height, which only exists once the skin is created.
     */
    public static void fitToRows(TableView<?> table) {
        table.setFixedCellSize(ROW_HEIGHT);
        table.skinProperty().addListener((obs, oldSkin, skin) -> {
            Region header = (Region) table.lookup(".column-header-background");
            table.prefHeightProperty().bind(Bindings.createDoubleBinding(
                    () -> header.getHeight() + table.getInsets().getTop() + table.getInsets().getBottom()
                            + table.getItems().size() * ROW_HEIGHT,
                    header.heightProperty(), table.insetsProperty(), table.getItems()));
        });
    }
}
