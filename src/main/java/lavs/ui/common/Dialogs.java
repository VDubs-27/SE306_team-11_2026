package lavs.ui.common;

import javafx.scene.Node;

/**
 * Small pop-up questions, styled like the rest of the app.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Dialogs {

    private static final String CONFIRM = "confirm";

    private Dialogs() {
    }

    /**
     * Asks the user to confirm something they can't undo, e.g. discarding a half-filled form.
     * Returns true only if they choose the confirm button.
     */
    public static boolean confirm(Node owner, String question, String detail, String confirmText) {
        String chosen = new Modal(owner, question)
                .message(detail)
                .button("keep", "Keep going", Modal.Kind.SECONDARY)
                .button(CONFIRM, confirmText, Modal.Kind.PRIMARY)
                .showAndWait();
        return CONFIRM.equals(chosen);
    }
}
