package lavs.ui.common;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/**
 * Short messages that appear at the top of the window for a few seconds, such as "Signed out":
 * a tinted card with a round icon, a title and a line of detail. They don't block the screen,
 * and a click dismisses one straight away.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Toasts {

    /** What kind of message it is, which sets its colour and icon. */
    public enum Kind {
        INFO("toast-info", "M0 -4.2v0.4 M0 -0.8v5"),
        SUCCESS("toast-success", "M-4.2 0.2l2.8 2.8l5.6-5.6"),
        WARNING("toast-warning", "M0 -4.6v5 M0 3.6v0.4"),
        ERROR("toast-error", "M-3.2 -3.2l6.4 6.4 M3.2 -3.2l-6.4 6.4");

        private final String styleClass;
        private final String glyph;

        Kind(String styleClass, String glyph) {
            this.styleClass = styleClass;
            this.glyph = glyph;
        }
    }

    private static final String LAYER_KEY = "lavs.toasts.layer";
    private static final Duration SHOW_FOR = Duration.seconds(4);
    private static final Duration FADE = Duration.millis(220);
    private static final double SLIDE = -12;
    private static final double ICON_RADIUS = 11;

    private Toasts() {
    }

    /**
     * Adds the layer toasts appear in to the window's root, on top of everything else. Clicks pass
     * through it except on a toast. Called once by the navigator when it sets up the window.
     */
    public static void install(Scene scene, StackPane windowRoot) {
        VBox layer = new VBox(10);
        layer.getStyleClass().add("toast-layer");
        layer.setAlignment(Pos.TOP_CENTER);
        layer.setPickOnBounds(false);
        windowRoot.getChildren().add(layer);
        scene.getProperties().put(LAYER_KEY, layer);
    }

    /** Shows a message over whatever screen is open; anyNode is any part of that window. */
    public static void show(Node anyNode, Kind kind, String title, String message) {
        show(anyNode.getScene(), kind, title, message);
    }

    /** Shows a message in this window, if it has a toast layer. */
    public static void show(Scene scene, Kind kind, String title, String message) {
        if (scene == null || !(scene.getProperties().get(LAYER_KEY) instanceof VBox layer)) {
            return;
        }
        HBox toast = toast(kind, title, message);
        layer.getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(FADE, toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        TranslateTransition slideIn = new TranslateTransition(FADE, toast);
        slideIn.setFromY(SLIDE);
        slideIn.setToY(0);
        FadeTransition fadeOut = new FadeTransition(FADE, toast);
        fadeOut.setToValue(0);
        SequentialTransition life = new SequentialTransition(
                new ParallelTransition(fadeIn, slideIn), new PauseTransition(SHOW_FOR), fadeOut);
        life.setOnFinished(event -> layer.getChildren().remove(toast));
        toast.setOnMouseClicked(event -> {
            life.stop();
            layer.getChildren().remove(toast);
        });
        life.play();
    }

    /** The card: a round icon on the left, then the title over the message. */
    private static HBox toast(Kind kind, String title, String message) {
        Circle disc = new Circle(ICON_RADIUS);
        disc.getStyleClass().add("toast-icon-disc");
        SVGPath glyph = new SVGPath();
        glyph.setContent(kind.glyph);
        glyph.getStyleClass().add("toast-icon-glyph");
        StackPane icon = new StackPane(disc, glyph);
        icon.setMinSize(ICON_RADIUS * 2, ICON_RADIUS * 2);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("toast-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("toast-message");
        messageLabel.setWrapText(true);

        HBox toast = new HBox(14, icon, new VBox(2, titleLabel, messageLabel));
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.getStyleClass().addAll("toast", kind.styleClass);
        return toast;
    }
}
