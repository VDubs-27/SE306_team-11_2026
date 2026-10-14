package lavs.logic.adapter;

import java.io.IOException;
import java.util.Objects;
import mainframe.client.Connection;
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

    <T extends Message> T send(T message) {
        Status status;
        try {
            status = message.send(connection);
        } catch (IOException e) {
            throw new MainframeException(StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode(), NETWORK_FAILURE_TEXT, e);
        }
        if (!status.getWasSuccessful()) {
            throw new MainframeException(status.getErrorCode(), describe(status));
        }
        return message;
    }

    private static String describe(Status status) {
        int code = status.getErrorCode();
        if (code == StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode()) {
            return NETWORK_FAILURE_TEXT;
        }
        String message = status.getErrorMessage();
        return message == null || message.isBlank() ? REJECTED_TEXT : message;
    }
}