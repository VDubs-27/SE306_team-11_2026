package lavs.logic.adapter;

import static lavs.logic.model.Validation.isBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

final class Values {

    private Values() {
    }

    static boolean isYes(String flag) {
        return "y".equalsIgnoreCase(flag);
    }

    static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    static BigDecimal decimal(String value) {
        return isBlank(value) ? null : new BigDecimal(value.trim());
    }

    // Salary is optional in LAVS, but the employer messages always send a number
    static double toDouble(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    static LocalDate date(String value) {
        return isBlank(value) ? null : LocalDate.parse(value.trim());
    }

    static String text(LocalDate date) {
        return date == null ? null : date.toString();
    }
}
