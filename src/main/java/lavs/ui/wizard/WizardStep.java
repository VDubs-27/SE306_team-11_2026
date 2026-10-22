package lavs.ui.wizard;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import javafx.scene.Node;

/**
 * One step of a wizard: its name in the stepper, what it shows, a check that runs before moving on
 * (showing any errors and returning false to stay put), and anything to refresh just before it shows.
 * AI-assisted: written by Jack, refined with Claude.
 */
public record WizardStep(String title, Node content, BooleanSupplier check, Runnable onShow) {

    public WizardStep {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        check = check == null ? () -> true : check;
        onShow = onShow == null ? () -> { } : onShow;
    }

    /** A step with nothing to check before moving on. */
    public WizardStep(String title, Node content) {
        this(title, content, null, null);
    }
}
