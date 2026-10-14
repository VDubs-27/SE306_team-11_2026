package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import lavs.logic.adapter.MainframeAdapter;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public class LoginService {

    static final String REJECTED_MESSAGE = "The user name or password is incorrect.";

    private final MainframeAdapter mainframe;
    private volatile String currentUser;

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

        currentUser = userName;
        return LoginResult.success();
    }

    public void logout() {
        mainframe.logout();
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
