package lavs.logic.adapter;

import java.io.IOException;
import java.util.Objects;
import mainframe.client.Connection;
import mainframe.client.Message;
import mainframe.client.Status;

public class MainframeAdapter {
    private static final String NETWORK_FAILURE_TEXT =
            "Could not reach the mainframe. Try again later.";
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
            throw new MainframeException(MainframeException.NETWORK_FAILURE, NETWORK_FAILURE_TEXT, e);
        }
        if (!status.getWasSuccessful()) {
            throw new MainframeException(status.getErrorCode(), describe(status));
        }
        return message;
    }

    private static String describe(Status status) {
        if (status.getErrorCode() == MainframeException.NETWORK_FAILURE) {
            return NETWORK_FAILURE_TEXT;
        }
        String message = status.getErrorMessage();
        return message == null || message.isBlank() ? REJECTED_TEXT : message;
    }
}