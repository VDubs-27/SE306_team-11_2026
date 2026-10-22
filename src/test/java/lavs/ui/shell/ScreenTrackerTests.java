package lavs.ui.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class ScreenTrackerTests {

    private final ScreenTracker tracker = new ScreenTracker();

    @Test
    void aLoadGetsATicketForTheCurrentScreen() {
        long ticket = tracker.startLoad();

        assertNotEquals(ScreenTracker.NO_LOAD, ticket);
        assertTrue(tracker.isAt(ticket));
    }

    @Test
    void aSecondLoadForTheSameScreenIsRefused() {
        tracker.startLoad();

        assertEquals(ScreenTracker.NO_LOAD, tracker.startLoad());
    }

    @Test
    void aFinishedLoadAllowsTheNext() {
        long ticket = tracker.startLoad();

        assertTrue(tracker.finishLoad(ticket));
        assertNotEquals(ScreenTracker.NO_LOAD, tracker.startLoad());
    }

    @Test
    void aLoadThatFinishesAfterTheUserMovedOnIsDropped() {
        long ticket = tracker.startLoad();
        tracker.moved();

        assertFalse(tracker.finishLoad(ticket));
    }

    @Test
    void movingOnAllowsANewLoadWhileTheOldOneIsStillRunning() {
        tracker.startLoad();
        tracker.moved();

        assertNotEquals(ScreenTracker.NO_LOAD, tracker.startLoad());
    }

    @Test
    void anOldLoadFinishingDoesNotReleaseTheNewOne() {
        long old = tracker.startLoad();
        tracker.moved();
        long current = tracker.startLoad();

        tracker.finishLoad(old);

        assertEquals(ScreenTracker.NO_LOAD, tracker.startLoad());
        assertTrue(tracker.finishLoad(current));
    }

    @Test
    void positionTellsWhetherTheUserHasMoved() {
        long before = tracker.position();

        assertTrue(tracker.isAt(before));
        tracker.moved();
        assertFalse(tracker.isAt(before));
    }

    @Test
    void aScreenThatWantsRefreshingIsRefreshed() {
        List<String> calls = new ArrayList<>();
        Object screen = new Object();
        tracker.remember(screen, (Refreshable) () -> calls.add("refreshed"));

        tracker.refresh(screen);

        assertEquals(List.of("refreshed"), calls);
    }

    @Test
    void aScreenThatDoesNotWantRefreshingIsLeftAlone() {
        Object screen = new Object();
        tracker.remember(screen, new Object());

        tracker.refresh(screen);
    }

    @Test
    void aForgottenScreenIsNotRefreshed() {
        List<String> calls = new ArrayList<>();
        Object screen = new Object();
        tracker.remember(screen, (Refreshable) () -> calls.add("refreshed"));

        tracker.forget(screen);
        tracker.refresh(screen);

        assertEquals(List.of(), calls);
    }

    @Test
    void forgettingEverythingStopsEveryRefresh() {
        List<String> calls = new ArrayList<>();
        Object first = new Object();
        Object second = new Object();
        tracker.remember(first, (Refreshable) () -> calls.add("first"));
        tracker.remember(second, (Refreshable) () -> calls.add("second"));

        tracker.forgetAll();
        tracker.refresh(first);
        tracker.refresh(second);

        assertEquals(List.of(), calls);
    }

    @Test
    void refreshingAScreenNeverRememberedDoesNothing() {
        tracker.refresh(new Object());
    }
}