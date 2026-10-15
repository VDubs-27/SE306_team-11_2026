package lavs.ui.common;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * Small helpers for parts of a screen.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Nodes {

    private Nodes() {
    }

    /** Shows or hides a part of the screen; a hidden part takes up no space, so nothing leaves a gap. */
    public static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /** Keeps a scrolling page at least as tall as the window, so a part of it set to grow can fill the rest. */
    public static void fillViewport(ScrollPane scroll, Region page) {
        page.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> scroll.getViewportBounds().getHeight(), scroll.viewportBoundsProperty()));
    }

    /** Cuts a region's contents to its rounded corners, e.g. so a table at a card's edge can't square them off. */
    public static void roundCorners(Region region, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }
}
