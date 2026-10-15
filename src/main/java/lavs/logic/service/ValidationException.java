package lavs.logic.service;

import lavs.logic.model.FieldError;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<FieldError> errors;

    public ValidationException(List<FieldError> errors) {
        super("Some fields are invalid.");
        this.errors = List.copyOf(errors);
    }

    public List<FieldError> errors() {
        return this.errors;
    }
}
