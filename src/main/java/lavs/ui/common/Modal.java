package lavs.ui.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
 * dialog, and returns the id of the button chosen; × and Escape count as {@link #CLOSED}. When the
 * window is too short for it, the content scrolls inside the card and the title and buttons stay put.
 * Enter presses Save on a form; a question (with no checks on its answer) needs a click or Space.
 * While busy (see setBusy), such as saving to the mainframe, nothing on the card can be used and it
 * can't be closed, until the work is done.
 * Used for the add/edit forms and for confirmations.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class Modal {

    /** What showAndWait() returns when the pop-up is closed with × or Escape. */
    public static final String CLOSED = "closed";

    /** How a button looks; danger buttons (such as Remove) sit on the left, apart from the rest. */
    public enum Kind { PRIMARY, SECONDARY, DANGER }

    private static final Duration FADE = Duration.millis(120);
    /** Space kept between the card and the window's edges. */
    private static final double WINDOW_MARGIN = 24;

    private final Node owner;
    private final VBox card;
    private final VBox body;
    private final HBox leftButtons = new HBox(10);
    private final HBox rightButtons = new HBox(10);
    private final Label errorText = new Label();
    private final HBox errorBanner;
    private final Button closeButton = new Button("✕");
    /** Each button by its id, with its own text, so a busy button's text can be put back. */
    private final Map<String, Button> buttons = new HashMap<>();
    private final Map<String, String> buttonTexts = new HashMap<>();
    private boolean busy;
    /** The button Enter presses: a form's Save, the one primary button with checks. */
    private Button enterButton;
    private final Object loopKey = new Object();
    private boolean open;

    public Modal(Node owner, String title) {
        this.owner = owner;

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("heading-2");
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        closeButton.getStyleClass().add("modal-close");
        closeButton.setOnAction(event -> finish(CLOSED));
        HBox header = new HBox(titleLabel, gap, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("modal-header");

        body = new VBox(12);
        body.getStyleClass().add("modal-body");

        Region footerGap = new Region();
        HBox.setHgrow(footerGap, Priority.ALWAYS);
        HBox footer = new HBox(leftButtons, footerGap, rightButtons);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("modal-footer");

        // Why the last try failed, e.g. the mainframe couldn't be reached; hidden until then
        Label errorIcon = new Label("ⓘ");
        errorIcon.setMinWidth(Region.USE_PREF_SIZE);
        errorText.setWrapText(true);
        errorBanner = new HBox(errorIcon, errorText);
        errorBanner.getStyleClass().add("banner-error");
        VBox.setMargin(errorBanner, new Insets(6, 24, 2, 24));
        Nodes.show(errorBanner, false);

        // The content scrolls when the window is too short for the whole card
        ScrollPane bodyScroll = new ScrollPane(body);
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.getStyleClass().add("modal-scroll");
        VBox.setVgrow(bodyScroll, Priority.ALWAYS);
        header.setMinHeight(Region.USE_PREF_SIZE);
        footer.setMinHeight(Region.USE_PREF_SIZE);
        errorBanner.setMinHeight(Region.USE_PREF_SIZE);

        card = new VBox(header, errorBanner, bodyScroll, footer);
        card.getStyleClass().add("modal-card");
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    /** Adds a form or other content to the card. */
    public Modal content(Node content) {
        body.getChildren().add(content);
        return this;
    }

    /** Shows why something failed in a red banner under the title, or hides it when message is null. */
    public void showError(String message) {
        errorText.setText(message == null ? "" : message);
        Nodes.show(errorBanner, message != null);
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
        if (kind == Kind.PRIMARY && guard != null) {
            enterButton = button;
        }
        buttons.put(id, button);
        buttonTexts.put(id, text);
        (kind == Kind.DANGER ? leftButtons : rightButtons).getChildren().add(button);
        return this;
    }

    /**
     * Locks the card while work it started is under way, e.g. saving: the content and buttons can't be
     * used, it can't be closed, and the button with this id says busyText, e.g. "Saving…".
     */
    public void setBusy(String id, String busyText) {
        busy = true;
        buttons.get(id).setText(busyText);
        lock(true);
    }

    /** Unlocks the card after setBusy, putting the buttons' own text back. */
    public void clearBusy() {
        busy = false;
        buttons.forEach((id, button) -> button.setText(buttonTexts.get(id)));
        lock(false);
    }

    /** Closes the pop-up as if the button with this id were chosen, e.g. once work started by it is done. */
    public void close(String id) {
        clearBusy();
        finish(id);
    }

    private void lock(boolean locked) {
        body.setDisable(locked);
        closeButton.setDisable(locked);
        buttons.values().forEach(button -> button.setDisable(locked));
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
        // The frame keeps the card inside the window, so a tall card gets shorter and scrolls
        VBox frame = new VBox(card);
        frame.setAlignment(Pos.CENTER);
        frame.setFillWidth(false);
        frame.setPadding(new Insets(WINDOW_MARGIN));
        frame.setPickOnBounds(false);
        StackPane layer = new StackPane(scrim, frame);
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
        // Enter reaches here once the focused control has had its turn; it never reaches the screen behind
        holder.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                event.consume();
                pressForEnter(event.getTarget());
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

    /**
     * What Enter does: presses a focused button; in a box with its own Enter (a note's text, or a
     * search box) does nothing more; anywhere else on a form, presses Save.
     */
    private void pressForEnter(Object target) {
        if (target instanceof ButtonBase focused) {
            focused.fire();
        } else if (target instanceof TextInputControl && !(target instanceof TextField)
                || target instanceof TextField field && field.getOnAction() != null) {
            return;
        } else if (enterButton != null && !enterButton.isDisabled()) {
            enterButton.fire();
        }
    }

    /** Ends the wait once; a second Escape or click while closing, or anything while busy, does nothing. */
    private void finish(String id) {
        if (open && !busy) {
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
