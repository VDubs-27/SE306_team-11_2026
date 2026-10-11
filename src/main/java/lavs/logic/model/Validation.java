package lavs.logic.model;

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
}
