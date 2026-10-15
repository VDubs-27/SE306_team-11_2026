package lavs.ui.common;

import java.util.function.BooleanSupplier;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * A pop-up drawn inside the app rather than as a separate window: the screen dims behind it and a
 * white card shows a title, the content and buttons. showAndWait() waits for an answer, like a
 * dialog, and returns the id of the button chosen; × and Escape count as {@link #CLOSED}.
 * Used for the add/edit forms and for confirmations.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Modal {

    /** What showAndWait() returns when the pop-up is closed with × or Escape. */
    public static final String CLOSED = "closed";

    /** How a button looks; danger buttons (such as Remove) sit on the left, apart from the rest. */
    public enum Kind { PRIMARY, SECONDARY, DANGER }

    private static final Duration FADE = Duration.millis(120);

    private final Node owner;
    private final VBox card;
    private final VBox body;
    private final HBox leftButtons = new HBox(10);
    private final HBox rightButtons = new HBox(10);
    private final Object loopKey = new Object();
    private boolean open;

    public Modal(Node owner, String title) {
        this.owner = owner;

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("heading-2");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        Button close = new Button("✕");
        close.getStyleClass().add("modal-close");
        close.setOnAction(event -> finish(CLOSED));
        HBox header = new HBox(titleLabel, gap, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("modal-header");

        body = new VBox(12);
        body.getStyleClass().add("modal-body");

        Region footerGap = new Region();
        HBox.setHgrow(footerGap, Priority.ALWAYS);
        HBox footer = new HBox(leftButtons, footerGap, rightButtons);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("modal-footer");

        card = new VBox(header, body, footer);
        card.getStyleClass().add("modal-card");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    /** Adds a form or other content to the card. */
    public Modal content(Node content) {
        body.getChildren().add(content);
        return this;
    }

    /** Adds a line of explanation, for confirmations. */
    public Modal message(String text) {
        Label message = new Label(text);
        message.setWrapText(true);
        message.getStyleClass().add("text-muted");
        message.setMaxWidth(380);
        body.getChildren().add(message);
        return this;
    }

    /** Adds a button that closes the pop-up and returns its id. */
    public Modal button(String id, String text, Kind kind) {
        return button(id, text, kind, null);
    }

    /**
     * Adds a button that closes the pop-up and returns its id, but only once guard passes
     * (for Save, guard checks the form and shows its errors). A null guard always passes.
     */
    public Modal button(String id, String text, Kind kind, BooleanSupplier guard) {
        Button button = new Button(text);
        button.getStyleClass().addAll(switch (kind) {
            case PRIMARY -> new String[] {"button-primary"};
            case SECONDARY -> new String[] {"button-secondary"};
            case DANGER -> new String[] {"button-secondary", "button-danger"};
        });
        button.setOnAction(event -> {
            if (guard == null || guard.getAsBoolean()) {
                finish(id);
            }
        });
        (kind == Kind.DANGER ? leftButtons : rightButtons).getChildren().add(button);
        return this;
    }

    /**
     * Dims the screen, shows the card and waits for the user to choose. Returns the chosen
     * button's id, or {@link #CLOSED}. The content is let go afterwards, so it can be shown again.
     */
    public String showAndWait() {
        Scene scene = owner.getScene();
        Parent screen = scene.getRoot();

        Region scrim = new Region();
        scrim.getStyleClass().add("modal-scrim");
        StackPane layer = new StackPane(scrim, card);
        // Laid over the screen by swapping in a holder as the scene's root, then putting it back
        StackPane holder = new StackPane();
        scene.setRoot(holder);
        holder.getChildren().addAll(screen, layer);
        holder.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                finish(CLOSED);
            }
        });

        layer.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(FADE, layer);
        fadeIn.setToValue(1);
        fadeIn.play();
        focusFirstInput();

        open = true;
        Object chosen = Platform.enterNestedEventLoop(loopKey);

        holder.getChildren().clear();
        scene.setRoot(screen);
        body.getChildren().clear();
        return (String) chosen;
    }

    /** Ends the wait once; a second Escape or click while closing does nothing. */
    private void finish(String id) {
        if (open) {
            open = false;
            Platform.exitNestedEventLoop(loopKey, id);
        }
    }

    /** Puts the cursor in the first field on the card, or on the card so Escape works straight away. */
    private void focusFirstInput() {
        Node first = firstInput(card);
        Node target = first != null ? first : card;
        Platform.runLater(target::requestFocus);
    }

    /** The first text box, drop-down or tick box, in the order they appear. */
    private static Node firstInput(Node node) {
        if (node instanceof TextInputControl || node instanceof ComboBoxBase<?> || node instanceof CheckBox) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node found = firstInput(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
