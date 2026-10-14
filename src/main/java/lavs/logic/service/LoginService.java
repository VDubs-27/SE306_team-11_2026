package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;
import lavs.logic.model.constants.Constants;

public class LoginService {

    static final String REJECTED_MESSAGE = "The user name or password is incorrect.";
    static final String CONSTANTS_FAILED_MESSAGE =
            "Signed in, but the list values could not be loaded from the mainframe. Try signing in again.";

    private final MainframeAdapter mainframe;
    private volatile String currentUser;
    private volatile Constants constants = Constants.EMPTY;

    public LoginService(MainframeAdapter mainframe) {
        this.mainframe = Objects.requireNonNull(mainframe, "mainframe");
    }

    public LoginResult login(String userName, String password) {
        List<FieldError> errors = new Validator()
                .required("userName", userName, "User name")
                .required("password", password, "Password")
                .errors();
        if (!errors.isEmpty()) {
            return LoginResult.invalid(errors);
        }

        try {
            mainframe.login(userName, password);
        } catch (MainframeException e) {
            return LoginResult.failed(e.isNetworkFailure() ? e.getMessage() : REJECTED_MESSAGE);
        }

        try {
            constants = mainframe.loadConstants();
        } catch (MainframeException e) {
            logout();
            return LoginResult.failed(e.isNetworkFailure() ? e.getMessage() : CONSTANTS_FAILED_MESSAGE);
        }

        currentUser = userName;
        return LoginResult.success();
    }

    public void logout() {
        mainframe.logout();
        currentUser = null;
        constants = Constants.EMPTY;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public Constants getConstants() {
        return constants;
    }
}
