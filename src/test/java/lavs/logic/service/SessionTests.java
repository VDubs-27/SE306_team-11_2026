package lavs.logic.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import org.junit.jupiter.api.Test;

class SessionTests {

    private static final Constants CONSTANTS = new Constants(Map.of(ConstantCategory.RATE_TYPE, List.of("Fixed")));

    private final Session session = new Session();

    @Test
    void startsSignedOut() {
        assertAll(
                () -> assertFalse(session.isLoggedIn()),
                () -> assertNull(session.currentUser()),
                () -> assertSame(Constants.EMPTY, session.constants())
        );
    }

    @Test
    void startHoldsTheUserAndConstants() {
        session.start("alice", CONSTANTS);

        assertAll(
                () -> assertTrue(session.isLoggedIn()),
                () -> assertEquals("alice", session.currentUser()),
                () -> assertSame(CONSTANTS, session.constants())
        );
    }

    @Test
    void endClearsTheUserAndConstants() {
        session.start("alice", CONSTANTS);

        session.end();

        assertAll(
                () -> assertFalse(session.isLoggedIn()),
                () -> assertNull(session.currentUser()),
                () -> assertSame(Constants.EMPTY, session.constants())
        );
    }

    @Test
    void startRequiresUserAndConstants() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> session.start(null, CONSTANTS)),
                () -> assertThrows(NullPointerException.class, () -> session.start("alice", null))
        );
    }
}
