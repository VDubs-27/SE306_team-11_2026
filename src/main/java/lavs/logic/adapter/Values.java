package lavs.logic.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static lavs.logic.model.Validation.isBlank;

final class Values {

    private Values() {
    }

    static boolean isYes(String flag) {
        return "y".equalsIgnoreCase(flag);
    }

    static BigDecimal decimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    // Salary is optional in LAVS, but the employer messages always send a number
    static double toDouble(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    static LocalDate date(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.contains("T")
                ? LocalDate.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                : LocalDate.parse(value);
    }
}
