package lavs.logic.service;

import java.util.List;
import lavs.logic.model.FieldError;

public record LoginResult(List<FieldError> fieldErrors, String errorMessage) {

    public LoginResult {
        fieldErrors = List.copyOf(fieldErrors);
    }

    public static LoginResult success() {
        return new LoginResult(List.of(), null);
    }

    public static LoginResult invalid(List<FieldError> fieldErrors) {
        return new LoginResult(fieldErrors, null);
    }

    public static LoginResult failed(String errorMessage) {
        return new LoginResult(List.of(), errorMessage);
    }

    public boolean successful() {
        return fieldErrors.isEmpty() && errorMessage == null;
    }
}
