package lavs.ui.shell;

import java.util.HashMap;
import java.util.Map;

/**
 * Tells a result that arrives late from one the user still wants. The position advances each time the
 * user moves to another screen; a background call keeps the position it started at, and its result is
 * only used if that is still the current one. Also stops a second load starting while one is running
 * for the same screen, and remembers which screens want refreshing when the user comes back to them.
 * Used on the JavaFX thread only.
 */
final class ScreenTracker {

    /** Returned by startLoad when a load is already running for the current screen. */
    static final long NO_LOAD = -1;

    private final Map<Object, Refreshable> refreshables = new HashMap<>();
    private long position;
    private long loadingAt = NO_LOAD;

    long position() {
        return position;
    }

    boolean isAt(long earlier) {
        return earlier == position;
    }

    /** The user moved to another screen, or the window changed. */
    void moved() {
        position++;
    }

    /** Starts a load for the current screen: its ticket, or NO_LOAD if one is already running for it. */
    long startLoad() {
        if (loadingAt == position) {
            return NO_LOAD;
        }
        loadingAt = position;
        return position;
    }

    /** Ends a load. True if the user is still on the screen it started from, so its result can be shown. */
    boolean finishLoad(long ticket) {
        if (ticket != position) {
            return false;
        }
        loadingAt = NO_LOAD;
        return true;
    }

    /** Notes a screen's controller, if it wants refreshing when the user comes back to the screen. */
    void remember(Object screen, Object controller) {
        if (controller instanceof Refreshable refreshable) {
            refreshables.put(screen, refreshable);
        }
    }

    /** Lets go of a screen the user has left for good. */
    void forget(Object screen) {
        refreshables.remove(screen);
    }

    void forgetAll() {
        refreshables.clear();
    }

    /** Refreshes the screen, if it asked to be. */
    void refresh(Object screen) {
        Refreshable stale = refreshables.get(screen);
        if (stale != null) {
            stale.refresh();
        }
    }
}