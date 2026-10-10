package lavs.model.customer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record NewCustomer(
    String title,
    String firstName,
    String familyName,
    LocalDate dateOfBirth,
    String nationality
) {
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (isBlank(firstName)) {
            errors.add("First name is required.");
        }

        if (isBlank(familyName)) {
            errors.add("Family name is required.");
        }

        return errors;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}