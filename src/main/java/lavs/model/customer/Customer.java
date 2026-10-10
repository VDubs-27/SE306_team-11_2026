package lavs.model.customer;

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
        Objects.requireNonNull(firstName, "firstName");
        Objects.requireNonNull(familyName, "familyName");
    }

    public String fullName() {
        return String.format("%s %s", firstName, familyName);
    }

    public Customer withStatus(CustomerStatus newStatus) {
        return new Customer(number, title, firstName, familyName, newStatus, dateOfBirth, nationality);
    }
}