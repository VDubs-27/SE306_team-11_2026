package lavs.ui.customer.profile;

import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/** Shared set-up for the tables in the profile tabs (Loans, Notes). */
final class ProfileTables {

    /** Row height from .table-row-cell in app.css. */
    private static final double ROW_HEIGHT = 52;
    /** Column header row plus the table's 1px border top and bottom, with a pixel spare. */
    private static final double HEADER_HEIGHT = 46;

    private ProfileTables() {
    }

    /** Draws the › in the last column and calls onOpen when a row is clicked. */
    static <T> void makeRowsOpenable(TableView<T> table, TableColumn<T, Void> arrowColumn, Consumer<T> onOpen) {
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
        table.setFixedCellSize(ROW_HEIGHT);
    }

    /** Shows the rows in a table just tall enough for them, or the empty message when there are none. */
    static <T> void show(TableView<T> table, Label emptyState, List<T> rows) {
        table.getItems().setAll(rows);
        table.setPrefHeight(HEADER_HEIGHT + rows.size() * ROW_HEIGHT);

        boolean hasRows = !rows.isEmpty();
        table.setVisible(hasRows);
        table.setManaged(hasRows);
        emptyState.setVisible(!hasRows);
        emptyState.setManaged(!hasRows);
    }
}
