package lavs.logic.adapter;

import mainframe.client.StatusMessage;

public class MainframeException extends RuntimeException {

    private final int errorCode;

    public MainframeException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MainframeException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int errorCode() {
        return errorCode;
    }

    public boolean isNetworkFailure() {
        return errorCode == StatusMessage.NETWORK_FAILURE_UNAVAILABLE.getCode();
    }

    public boolean isServerError() {
        return errorCode == StatusMessage.SERVER_FAILURE.getCode();
    }
}