package lavs.ui.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

import lavs.logic.service.Result;

/**
 * Runs mainframe work off the screen's thread, so the window stays responsive while it waits, then
 * hands the result back on the screen's thread. Work runs one piece at a time, in the order asked
 * for, as the mainframe connection handles one request at a time. While any is waiting or running,
 * busy is true, and the header's loading bar sweeps.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Background {

    /** Shown when work fails in a way the services don't report, such as a bug. */
    public static final String UNEXPECTED = "Something went wrong. Try again, and if it keeps happening, restart LAVS.";

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(work -> {
        Thread thread = new Thread(work, "mainframe");
        thread.setDaemon(true);
        return thread;
    });
    private static final ReadOnlyBooleanWrapper BUSY = new ReadOnlyBooleanWrapper(false);
    private static final System.Logger LOG = System.getLogger(Background.class.getName());

    private static int running;

    private Background() {
    }

    /** True while any work is waiting or running. */
    public static ReadOnlyBooleanProperty busyProperty() {
        return BUSY.getReadOnlyProperty();
    }

    /**
     * Runs work, then gives its result to onDone on the screen's thread. If the work throws, onDone gets
     * a failed result saying something went wrong instead, so a screen never waits forever.
     * Call from the screen's thread.
     */
    public static <T> void run(Supplier<Result<T>> work, Consumer<Result<T>> onDone) {
        changeRunning(1);
        WORKER.execute(() -> {
            Result<T> result = attempt(work);
            Platform.runLater(() -> {
                changeRunning(-1);
                onDone.accept(result);
            });
        });
    }

    private static <T> Result<T> attempt(Supplier<Result<T>> work) {
        try {
            return work.get();
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Mainframe work failed unexpectedly", e);
            return Result.failed(UNEXPECTED);
        }
    }

    private static void changeRunning(int by) {
        running += by;
        BUSY.set(running > 0);
    }
}
