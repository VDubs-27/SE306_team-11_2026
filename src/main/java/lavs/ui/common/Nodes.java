package lavs.ui.common;

import javafx.scene.Node;

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
}
