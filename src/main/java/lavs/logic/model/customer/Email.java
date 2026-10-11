package lavs.logic.model.customer;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record Email(
    long id,
    CustomerNumber customerNumber,
    Type type,
    String otherType,
    String address,
    boolean isPrimary
) {
    public Email {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(type, otherType, address, isPrimary);
    }

    public enum Type {
        PERSONAL("Personal"),
        WORK("Work"),
        OTHER("Other"),
        UNKNOWN("");

        private final String code;

        Type(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        public static Type fromCode(String code) {
            return Codes.fromCode(values(), Type::code, code, UNKNOWN);
        }
    }

    public record Summary(
        long id,
        CustomerNumber customerNumber,
        Type type,
        String address,
        boolean isPrimary
    ) {
        public Summary {
            Objects.requireNonNull(customerNumber, "customerNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        Type type,
        String otherType,
        String address,
        boolean isPrimary
    ) {
        private static final int ADDRESS_MAX_LENGTH = 60;

        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Email type")
                .check(type == Type.UNKNOWN, "type", "Email type must be one of the listed options.")
                .check(type == Type.OTHER && isBlank(otherType), "otherType", "Other email type is required.")
                .required("address", address, "Email address")
                .emailAddress("address", address, "Email address")
                .maxLength("address", address, ADDRESS_MAX_LENGTH, "Email address")
                .errors();
        }
    }
}
