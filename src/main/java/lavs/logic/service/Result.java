package lavs.logic.service;

import java.util.List;
import java.util.function.Supplier;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;

public record Result<T>(T value, List<FieldError> fieldErrors, String errorMessage) {

    public Result {
        fieldErrors = List.copyOf(fieldErrors);
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, List.of(), null);
    }

    public static <T> Result<T> invalid(List<FieldError> fieldErrors) {
        return new Result<>(null, fieldErrors, null);
    }

    public static <T> Result<T> failed(String errorMessage) {
        return new Result<>(null, List.of(), errorMessage);
    }

    public boolean successful() {
        return fieldErrors.isEmpty() && errorMessage == null;
    }

    static <T> Result<T> attempt(Supplier<Result<T>> action) {
        try {
            return action.get();
        } catch (MainframeException e) {
            return failed(e.getMessage());
        }
    }
}
