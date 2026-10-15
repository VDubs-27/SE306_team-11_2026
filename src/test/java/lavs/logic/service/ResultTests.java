package lavs.logic.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Test;

class ResultTests {

    private static final FieldError ERROR = new FieldError("name", "Name is required.");

    @Test
    void successCarriesTheValue() {
        Result<String> result = Result.success("saved");

        assertAll(
                () -> assertTrue(result.successful()),
                () -> assertEquals("saved", result.value())
        );
    }

    @Test
    void invalidCarriesFieldErrors() {
        Result<String> result = Result.invalid(List.of(ERROR));

        assertAll(
                () -> assertFalse(result.successful()),
                () -> assertEquals(List.of(ERROR), result.fieldErrors()),
                () -> assertNull(result.value())
        );
    }

    @Test
    void failedCarriesTheMessage() {
        Result<String> result = Result.failed("Mainframe unavailable");

        assertAll(
                () -> assertFalse(result.successful()),
                () -> assertEquals("Mainframe unavailable", result.errorMessage())
        );
    }

    @Test
    void fieldErrorsAreCopied() {
        List<FieldError> errors = new ArrayList<>(List.of(ERROR));
        Result<String> result = Result.invalid(errors);

        errors.clear();

        assertEquals(List.of(ERROR), result.fieldErrors());
    }

    @Test
    void attemptTurnsMainframeErrorsIntoFailures() {
        Result<String> result = Result.attempt(() -> {
            throw new MainframeException(3, "Customer not found");
        });

        assertEquals("Customer not found", result.errorMessage());
    }
}
