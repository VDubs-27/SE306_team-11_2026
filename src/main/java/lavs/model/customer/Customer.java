package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.time.LocalDate;
import java.util.Objects;

public record Customer(
    CustomerNumber number,
    String title,
    String firstName,
    String familyName,
    CustomerStatus status,
    LocalDate dateOfBirth,
    String nationality
) {
    public Customer {
        Objects.requireNonNull(number, "number");
        if (isBlank(firstName)) {
            throw new IllegalArgumentException("First name must not be null or blank.");
        }
        if (isBlank(familyName)) {
            throw new IllegalArgumentException("Family name must not be null or blank.");
        }
    }

    public String fullName() {
        return String.format("%s %s", firstName, familyName);
    }

    public Customer withStatus(CustomerStatus newStatus) {
        return new Customer(number, title, firstName, familyName, newStatus, dateOfBirth, nationality);
    }
}