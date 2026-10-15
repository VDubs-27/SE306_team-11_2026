package lavs.ui.common;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * What a screen shows when it couldn't load, e.g. the mainframe was unreachable: a card with the
 * error icon, what couldn't be loaded and why, then Try again and Back.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class ErrorPanel {

    private ErrorPanel() {
    }

    /**
     * A full screen of its own, e.g. title "Couldn't load this customer" and message the reason.
     * retry runs the load again; back leaves the screen.
     */
    public static VBox create(String title, String message, Runnable retry, Runnable back) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("error-panel-title");
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("error-panel-message");
        messageLabel.setWrapText(true);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button-secondary");
        backButton.setOnAction(event -> back.run());
        Button retryButton = new Button("Try again");
        retryButton.getStyleClass().add("button-primary");
        retryButton.setOnAction(event -> retry.run());
        HBox buttons = new HBox(12, backButton, retryButton);
        buttons.setAlignment(Pos.CENTER);

        StackPane icon = Toasts.icon(Toasts.Kind.ERROR);
        icon.getStyleClass().add("error-panel-icon");
        VBox card = new VBox(12, icon, titleLabel, messageLabel, buttons);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("card", "error-panel");
        card.setMaxWidth(520);

        VBox page = new VBox(card);
        page.setAlignment(Pos.TOP_CENTER);
        page.getStyleClass().add("page");
        return page;
    }
}
