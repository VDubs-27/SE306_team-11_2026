package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Customer(
    CustomerNumber number,
    String title,
    String firstName,
    String familyName,
    Status status,
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

    public Customer withStatus(Status newStatus) {
        return new Customer(number, title, firstName, familyName, newStatus, dateOfBirth, nationality);
    }

    public Details details() {
        return new Details(title, firstName, familyName, dateOfBirth, nationality);
    }

    public enum Status {
        ACTIVE,
        INACTIVE
    }

    public record Summary(
        CustomerNumber number,
        String title,
        String firstName,
        String familyName,
        LocalDate dateOfBirth,
        Status status
    ) {
        public Summary {
            Objects.requireNonNull(number, "number");
        }

        public String fullName() {
            return String.format("%s %s", firstName, familyName);
        }
    }

    public record Details(
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
}
