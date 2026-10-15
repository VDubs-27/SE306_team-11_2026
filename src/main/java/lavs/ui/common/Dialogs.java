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
     * Asks the user to confirm an action, e.g. removing someone from a loan, with Cancel beside the
     * confirm button. Returns true only if they choose the confirm button.
     */
    public static boolean confirm(Node owner, String question, String detail, String confirmText) {
        return ask(owner, question, detail, "Cancel", confirmText);
    }

    /**
     * Asks before throwing away unsaved work, e.g. leaving a half-filled wizard: "Keep going" stays,
     * "Discard" leaves. Returns true only if they choose to discard.
     */
    public static boolean confirmDiscard(Node owner, String question, String detail) {
        return ask(owner, question, detail, "Keep going", "Discard");
    }

    private static boolean ask(Node owner, String question, String detail, String stayText, String confirmText) {
        String chosen = new Modal(owner, question)
                .message(detail)
                .button("stay", stayText, Modal.Kind.SECONDARY)
                .button(CONFIRM, confirmText, Modal.Kind.PRIMARY)
                .showAndWait();
        return CONFIRM.equals(chosen);
    }
}
