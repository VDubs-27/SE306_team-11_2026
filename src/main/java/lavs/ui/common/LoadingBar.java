package lavs.ui.common;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Turns a thin bar, such as the red line along the top of the login screen, into a "working on it"
 * indicator: while running, a bright highlight sweeps along it from left to right, over and over.
 * It doesn't show progress, only that something is happening.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class LoadingBar {

    /** How long one sweep takes to cross the bar. */
    private static final Duration SWEEP_TIME = Duration.seconds(1.3);
    /** The highlight is this share of the bar's width, but never narrower than MIN_SWEEP_WIDTH. */
    private static final double SWEEP_SHARE = 0.3;
    private static final double MIN_SWEEP_WIDTH = 120;

    private final Pane bar;
    private final Region sweep = new Region();
    private final Timeline timeline = new Timeline();

    /** bar is the line to animate; it keeps its own colour, and the highlight is drawn over it. */
    public LoadingBar(Pane bar) {
        this.bar = bar;
        sweep.getStyleClass().add("loading-sweep");
        sweep.setManaged(false);
        sweep.setVisible(false);
        bar.getChildren().add(sweep);

        // The highlight starts and ends off the bar's edges, so it's cut to the bar's shape
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(bar.widthProperty());
        clip.heightProperty().bind(bar.heightProperty());
        bar.setClip(clip);
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /** Starts the sweep, sized to the bar as it is now. */
    public void start() {
        double width = bar.getWidth();
        double sweepWidth = Math.max(MIN_SWEEP_WIDTH, width * SWEEP_SHARE);
        sweep.resizeRelocate(0, 0, sweepWidth, bar.getHeight());
        timeline.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO, new KeyValue(sweep.translateXProperty(), -sweepWidth)),
                new KeyFrame(SWEEP_TIME, new KeyValue(sweep.translateXProperty(), width, Interpolator.EASE_BOTH)));
        sweep.setVisible(true);
        timeline.playFromStart();
    }

    /** Stops the sweep and leaves the bar plain again. */
    public void stop() {
        timeline.stop();
        sweep.setVisible(false);
    }
}
