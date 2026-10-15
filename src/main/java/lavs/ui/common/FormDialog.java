package lavs.ui.common;

import java.util.function.BooleanSupplier;

import javafx.scene.Node;

/**
 * Shows a form (an address, phone, email or employer) in a pop-up with Cancel and Save, and Remove
 * when editing. Save only closes the pop-up once the form's check passes, so errors show in place.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class FormDialog {

    /** What the user chose. */
    public enum Result { SAVED, REMOVED, CANCELLED }

    private static final String SAVE = "save";
    private static final String REMOVE = "remove";

    private FormDialog() {
    }

    /**
     * Shows the form and waits. check() runs when Save is pressed: it shows any errors and returns
     * false to keep the pop-up open. canRemove adds a Remove button for an existing record.
     */
    public static Result show(Node owner, String title, Node form, BooleanSupplier check, boolean canRemove) {
        Modal modal = new Modal(owner, title).content(form);
        if (canRemove) {
            modal.button(REMOVE, "Remove", Modal.Kind.DANGER);
        }
        modal.button("cancel", "Cancel", Modal.Kind.SECONDARY)
                .button(SAVE, "Save", Modal.Kind.PRIMARY, check);

        String chosen = modal.showAndWait();
        if (SAVE.equals(chosen)) {
            return Result.SAVED;
        }
        return REMOVE.equals(chosen) ? Result.REMOVED : Result.CANCELLED;
    }
}
