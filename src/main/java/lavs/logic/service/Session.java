package lavs.logic.service;

import java.util.Objects;
import lavs.logic.model.constants.Constants;

// The signed-in user and the constants loaded for them; LoginService fills it and screens read from it
public class Session {

    private static final State SIGNED_OUT = new State(null, Constants.EMPTY);

    // One immutable state, so a reader never sees a new user with the previous user's constants
    private volatile State state = SIGNED_OUT;

    public boolean isLoggedIn() {
        return state.user() != null;
    }

    public String currentUser() {
        return state.user();
    }

    public Constants constants() {
        return state.constants();
    }

    void start(String user, Constants constants) {
        state = new State(Objects.requireNonNull(user, "user"), Objects.requireNonNull(constants, "constants"));
    }

    void end() {
        state = SIGNED_OUT;
    }

    private record State(String user, Constants constants) {
    }
}
