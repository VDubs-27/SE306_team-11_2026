package lavs.logic.service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;
import mainframe.client.Connection;
import mainframe.client.Status;
import mainframe.client.StatusMessage;

public class LoginService {

    static final String NETWORK_FAILURE_MESSAGE = "Could not reach the mainframe. Check the connection and try again.";
    static final String REJECTED_MESSAGE = "The user name or password is incorrect.";

    private final Connection connection;
    private volatile String currentUser;

    public LoginService(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    public LoginResult login(String userName, String password) {
        List<FieldError> errors = new Validator()
                .required("userName", userName, "User name")
                .required("password", password, "Password")
                .errors();
        if (!errors.isEmpty()) {
            return LoginResult.invalid(errors);
        }

        Status status;
        try {
            status = connection.Login(userName, password);
        } catch (IOException e) {
            return LoginResult.failed(NETWORK_FAILURE_MESSAGE);
        }
        if (!status.getWasSuccessful()) {
            return LoginResult.failed(describe(status));
        }

        currentUser = userName;
        return LoginResult.success();
    }

    public void logout() {
        connection.Logout();
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    private static String describe(Status status) {
        if (status.getErrorCode().equals(StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode())) {
            return NETWORK_FAILURE_MESSAGE;
        }
        return REJECTED_MESSAGE;
    }
}
