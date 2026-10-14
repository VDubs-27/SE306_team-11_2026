package lavs.ui.common;

import java.util.function.BooleanSupplier;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * Shows a form (an address, phone, email or employer) in a pop-up with Cancel and Save, and Remove
 * when editing. Save only closes the pop-up once the form's check passes, so errors show in place.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class FormDialog {

    /** What the user chose. */
    public enum Result { SAVED, REMOVED, CANCELLED }

    private FormDialog() {
    }

    /**
     * Shows the form and waits. check() runs when Save is pressed: it shows any errors and returns
     * false to keep the pop-up open. canRemove adds a Remove button for an existing record.
     */
    public static Result show(Node owner, String title, Node form, BooleanSupplier check, boolean canRemove) {
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType remove = new ButtonType("Remove", ButtonBar.ButtonData.LEFT);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("LAVS");
        dialog.initOwner(owner.getScene().getWindow());
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().setAll(owner.getScene().getStylesheets());
        pane.getStyleClass().add("form-dialog");
        pane.setHeaderText(title);
        pane.setContent(form);
        pane.getButtonTypes().setAll(canRemove ? new ButtonType[] {remove, cancel, save} : new ButtonType[] {cancel, save});

        pane.lookupButton(save).getStyleClass().add("button-primary");
        pane.lookupButton(cancel).getStyleClass().add("button-secondary");
        pane.lookupButton(save).addEventFilter(ActionEvent.ACTION, event -> {
            if (!check.getAsBoolean()) {
                event.consume();
                // Grow to fit the error messages that just appeared
                pane.getScene().getWindow().sizeToScene();
            }
        });
        if (canRemove) {
            pane.lookupButton(remove).getStyleClass().addAll("button-secondary", "button-danger");
        }

        ButtonType chosen = dialog.showAndWait().orElse(cancel);
        // Let go of the form so it can be shown again in the next pop-up
        pane.setContent(null);
        if (chosen == save) {
            return Result.SAVED;
        }
        return chosen == remove ? Result.REMOVED : Result.CANCELLED;
    }
}
