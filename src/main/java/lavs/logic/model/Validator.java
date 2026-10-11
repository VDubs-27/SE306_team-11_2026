package lavs.logic.model;

import static lavs.logic.model.Validation.exceedsLength;
import static lavs.logic.model.Validation.isBlank;
import static lavs.logic.model.Validation.isEmailAddress;
import static lavs.logic.model.Validation.isNegative;
import static lavs.logic.model.Validation.isNumeric;
import static lavs.logic.model.Validation.isPastDate;
import static lavs.logic.model.Validation.isPositive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Validator {

    private final List<FieldError> errors = new ArrayList<>();

    public Validator required(String field, String value, String label) {
        return check(isBlank(value), field, label + " is required.");
    }

    public Validator required(String field, Object value, String label) {
        return check(value == null, field, label + " is required.");
    }

    public Validator maxLength(String field, String value, int max, String label) {
        return check(exceedsLength(value, max), field, label + " must be " + max + " characters or fewer.");
    }

    public Validator numeric(String field, String value, String label) {
        return check(!isBlank(value) && !isNumeric(value), field, label + " must contain only digits.");
    }

    public Validator emailAddress(String field, String value, String label) {
        return check(!isBlank(value) && !isEmailAddress(value), field, label + " must be in the format name@example.com.");
    }

    public Validator pastDate(String field, LocalDate value, LocalDate today, String label) {
        return check(value != null && !isPastDate(value, today), field, label + " must be in the past.");
    }

    public Validator positive(String field, BigDecimal value, String label) {
        return check(value != null && !isPositive(value), field, label + " must be greater than 0.");
    }

    public Validator notNegative(String field, BigDecimal value, String label) {
        return check(isNegative(value), field, label + " must be 0 or more.");
    }

    public Validator check(boolean failed, String field, String message) {
        if (failed) {
            errors.add(new FieldError(field, message));
        }
        return this;
    }

    public List<FieldError> errors() {
        return List.copyOf(errors);
    }
}
