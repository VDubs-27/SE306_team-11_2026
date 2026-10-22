package lavs.ui.shell;

/**
 * Tells a result that arrives late from one the user still wants. The position advances each time the
 * user moves to another screen; a background call keeps the position it started at, and its result is
 * only used if that is still the current one. Also stops a second load starting while one is running
 * for the same screen. Used on the JavaFX thread only.
 */
final class ScreenTracker {

    /** Returned by startLoad when a load is already running for the current screen. */
    static final long NO_LOAD = -1;

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
}