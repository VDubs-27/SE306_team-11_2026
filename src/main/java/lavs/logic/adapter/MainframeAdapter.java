package lavs.logic.adapter;

import mainframe.client.Connection;
import mainframe.client.MessageRequest;
import mainframe.client.MessageResponse;

import java.util.Objects;

public class MainframeAdapter {
    private final Connection connection;

    public MainframeAdapter(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    private MessageResponse send(MessageRequest request) {
        MessageResponse response = connection.send(request);
        mainframe.client.Status status = response.getStatus();
        if (status.getWasSuccessful()) {
            return response;
        }
        throw new MainframeException(status.getErrorCode(), describe(status));
    }

    private static String describe(mainframe.client.Status status) {
        if (status.getErrorCode() == MainframeException.NETWORK_FAILURE) {
            return "Could not reach the mainframe. Check the connection and try again.";
        }
        String message = status.getErrorMessage();
        return message == null || message.isBlank()
                ? "The mainframe rejected the request."
                : message;
    }
}