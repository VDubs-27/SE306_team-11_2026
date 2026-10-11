package lavs.model.customer;

import java.time.LocalDate;
import java.util.Objects;

public record CustomerSummary(
    CustomerNumber number,
    String title,
    String firstName,
    String familyName,
    LocalDate dateOfBirth,
    CustomerStatus status
) {
    public CustomerSummary {
        Objects.requireNonNull(number, "number");
    }

    public String fullName() {
        return String.format("%s %s", firstName, familyName);
    }
}
