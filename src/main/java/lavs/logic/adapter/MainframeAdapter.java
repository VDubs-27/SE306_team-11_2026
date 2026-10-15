package lavs.logic.adapter;

import java.io.IOException;
import java.util.Objects;
import lavs.logic.model.constants.Constants;
import mainframe.client.Connection;
import mainframe.client.HttpConnection;
import mainframe.client.Message;
import mainframe.client.Status;
import mainframe.client.StatusMessage;

public class MainframeAdapter {
    private static final String NETWORK_FAILURE_TEXT =
            "Could not reach the mainframe. Check the connection and try again.";
    private static final String REJECTED_TEXT = "The mainframe rejected the request.";

    private final Connection connection;

    public MainframeAdapter(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    // Connects to the mainframe over HTTP; the app starts here, since only the adapter may use mainframe.client
    public static MainframeAdapter connect(String baseUrl) {
        return new MainframeAdapter(new HttpConnection(baseUrl));
    }

    // Goes through Connection.Login rather than sending a Login message, because the
    // connection keeps the session token and transaction number from a successful login.
    public void login(String userName, String password) {
        check(() -> connection.Login(userName, password));
    }

    public void logout() {
        connection.Logout();
    }

    public Constants loadConstants() {
        return new ConstantsReader(this).read();
    }

    <T extends Message> T send(T message) {
        check(() -> message.send(connection));
        return message;
    }

    private static void check(MainframeCall call) {
        Status status;
        try {
            status = call.run();
        } catch (IOException e) {
            throw new MainframeException(StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode(), NETWORK_FAILURE_TEXT, e);
        }
        if (!status.getWasSuccessful()) {
            throw new MainframeException(status.getErrorCode(), describe(status));
        }
    }

    private static String describe(Status status) {
        int code = status.getErrorCode();
        if (code == StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode()) {
            return NETWORK_FAILURE_TEXT;
        }
        String message = status.getErrorMessage();
        return message == null || message.isBlank() ? REJECTED_TEXT : message;
    }

    @FunctionalInterface
    private interface MainframeCall {
        Status run() throws IOException;
    }
}
