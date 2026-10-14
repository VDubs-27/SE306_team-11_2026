package lavs.ui.common;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/**
 * Small pop-up questions, styled like the rest of the app.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Dialogs {

    private Dialogs() {
    }

    /**
     * Asks the user to confirm something they can't undo, e.g. discarding a half-filled form.
     * Returns true only if they choose the confirm button.
     */
    public static boolean confirm(Node owner, String question, String detail, String confirmText) {
        ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType keep = new ButtonType("Keep going", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, detail, keep, confirm);
        alert.setTitle("LAVS");
        alert.setHeaderText(question);
        alert.setGraphic(null);
        alert.initOwner(owner.getScene().getWindow());
        alert.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        alert.getDialogPane().lookupButton(confirm).getStyleClass().add("button-primary");
        alert.getDialogPane().lookupButton(keep).getStyleClass().add("button-secondary");
        return alert.showAndWait().filter(confirm::equals).isPresent();
    }
}
