package lavs.logic.adapter;

public class MainframeException extends RuntimeException {
    public static final int SERVER_ERROR = 3;
    public static final int NETWORK_FAILURE = 10;

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
        return errorCode == NETWORK_FAILURE;
    }

    public boolean isServerError() {
        return errorCode == SERVER_ERROR;
    }
}