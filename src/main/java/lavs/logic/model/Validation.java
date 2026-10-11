package lavs.logic.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Validation {

    private Validation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean exceedsLength(String value, int max) {
        return value != null && value.length() > max;
    }

    public static boolean isNumeric(String value) {
        return value != null && value.matches("[0-9]+");
    }

    public static boolean isEmailAddress(String value) {
        return value != null && value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    public static boolean isPastDate(LocalDate value, LocalDate today) {
        return value != null && value.isBefore(today);
    }

    public static boolean isOnOrBefore(LocalDate value, LocalDate other) {
        return value != null && other != null && !value.isAfter(other);
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.signum() > 0;
    }

    public static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }
}
