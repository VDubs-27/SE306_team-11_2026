package lavs.logic.service;

import java.util.ArrayList;
import java.util.List;
import lavs.logic.model.FieldError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoginResultTests {

    @Test
    void successHasNoErrors() {
        LoginResult result = LoginResult.success();

        Assertions.assertAll(
                () -> Assertions.assertTrue(result.successful()),
                () -> Assertions.assertTrue(result.fieldErrors().isEmpty()),
                () -> Assertions.assertNull(result.errorMessage())
        );
    }

    @Test
    void invalidHoldsFieldErrors() {
        List<FieldError> errors = List.of(new FieldError("userName", "User name is required."));

        LoginResult result = LoginResult.invalid(errors);

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertEquals(errors, result.fieldErrors()),
                () -> Assertions.assertNull(result.errorMessage())
        );
    }

    @Test
    void failedHoldsErrorMessage() {
        LoginResult result = LoginResult.failed("The user name or password is incorrect.");

        Assertions.assertAll(
                () -> Assertions.assertFalse(result.successful()),
                () -> Assertions.assertTrue(result.fieldErrors().isEmpty()),
                () -> Assertions.assertEquals("The user name or password is incorrect.", result.errorMessage())
        );
    }

    @Test
    void fieldErrorsAreCopied() {
        List<FieldError> errors = new ArrayList<>();
        errors.add(new FieldError("userName", "User name is required."));

        LoginResult result = LoginResult.invalid(errors);
        errors.clear();

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, result.fieldErrors().size()),
                () -> Assertions.assertThrows(UnsupportedOperationException.class,
                        () -> result.fieldErrors().add(new FieldError("password", "Password is required.")))
        );
    }

    @Test
    void requiresFieldErrors() {
        Assertions.assertThrows(NullPointerException.class, () -> new LoginResult(null, null));
    }
}
