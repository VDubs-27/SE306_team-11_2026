package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record CustomerDetails(
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
}
