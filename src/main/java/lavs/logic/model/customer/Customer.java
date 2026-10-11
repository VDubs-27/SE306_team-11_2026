package lavs.logic.model.customer;

import static lavs.logic.model.Validation.isBlank;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

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
        if (newStatus == null || newStatus == Status.UNKNOWN) {
            throw new IllegalArgumentException("Customer status must be ACTIVE or INACTIVE.");
        }
        return new Customer(number, title, firstName, familyName, newStatus, dateOfBirth, nationality);
    }

    public Details details() {
        return new Details(title, firstName, familyName, dateOfBirth, nationality);
    }

    public enum Status {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        UNKNOWN("");

        private final String code;

        Status(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Status fromCode(String code) {
            return Codes.fromCode(values(), Status::code, code, UNKNOWN);
        }
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
        private static final int TITLE_MAX_LENGTH = 10;
        private static final int NAME_MAX_LENGTH = 40;

        public List<FieldError> validate(LocalDate today) {
            return new Validator()
                .maxLength("title", title, TITLE_MAX_LENGTH, "Title")
                .required("firstName", firstName, "First name")
                .maxLength("firstName", firstName, NAME_MAX_LENGTH, "First name")
                .required("familyName", familyName, "Family name")
                .maxLength("familyName", familyName, NAME_MAX_LENGTH, "Family name")
                .pastDate("dateOfBirth", dateOfBirth, today, "Date of birth")
                .maxLength("nationality", nationality, NAME_MAX_LENGTH, "Nationality")
                .errors();
        }
    }
}
