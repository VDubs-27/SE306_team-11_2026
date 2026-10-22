package lavs.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javafx.application.Platform;

import lavs.logic.service.Result;
import lavs.ui.common.Background;

/**
 * Runs test steps on the JavaFX thread, headless (see pom.xml), and waits for background work to
 * come back, so a test can act as the user would and then check what the screen shows.
 */
public final class FxTesting {

    private static final long TIMEOUT_SECONDS = 10;

    private FxTesting() {
    }

    public static void start() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // another test class started it
        }
    }

    /** Runs action on the JavaFX thread and gives back what it returns, or rethrows what it threw. */
    public static <T> T get(Supplier<T> action) {
        if (Platform.isFxApplicationThread()) {
            return action.get(); // waiting for its own thread would never end
        }
        CompletableFuture<T> done = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                done.complete(action.get());
            } catch (RuntimeException | Error e) {
                done.completeExceptionally(e);
            }
        });
        return await(done);
    }

    public static void run(Runnable action) {
        get(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Waits until no background work is left, including work started by answers that came back while
     * waiting, such as the page a reload moves to after the first one it read.
     */
    public static void settle() {
        boolean busy;
        do {
            CompletableFuture<Boolean> done = new CompletableFuture<>();
            // Work runs in order, so this comes back after everything already asked for
            run(() -> Background.run(() -> Result.success(true),
                    ignored -> done.complete(Background.busyProperty().get())));
            busy = await(done);
        } while (busy);
    }

    private static <T> T await(CompletableFuture<T> done) {
        try {
            return done.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (e.getCause() instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException | TimeoutException e) {
            throw new IllegalStateException("JavaFX didn't finish in time", e);
        }
    }
}
