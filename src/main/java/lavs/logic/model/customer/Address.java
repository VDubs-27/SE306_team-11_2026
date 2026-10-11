package lavs.logic.model.customer;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record Address(
    long id,
    CustomerNumber customerNumber,
    Type type,
    String otherType,
    String line1,
    String line2,
    String suburb,
    String city,
    String postCode,
    String country,
    boolean isPrimary,
    boolean isMailing
) {
    public Address {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(
            type, otherType, line1, line2, suburb, city, postCode, country, isPrimary, isMailing);
    }

    public enum Type {
        RESIDENTIAL("Residential"),
        POSTAL("Postal"),
        FORWARDING("Forwarding"),
        DELEGATED("Delegated"),
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
        String city,
        String country,
        boolean isPrimary,
        boolean isMailing
    ) {
        public Summary {
            Objects.requireNonNull(customerNumber, "customerNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        Type type,
        String otherType,
        String line1,
        String line2,
        String suburb,
        String city,
        String postCode,
        String country,
        boolean isPrimary,
        boolean isMailing
    ) {
        private static final int TEXT_MAX_LENGTH = 60;

        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Address type")
                .check(type == Type.UNKNOWN, "type", "Address type must be one of the listed options.")
                .check(type == Type.OTHER && isBlank(otherType), "otherType", "Other address type is required.")
                .maxLength("otherType", otherType, TEXT_MAX_LENGTH, "Other address type")
                .required("line1", line1, "Address line 1")
                .maxLength("line1", line1, TEXT_MAX_LENGTH, "Address line 1")
                .maxLength("line2", line2, TEXT_MAX_LENGTH, "Address line 2")
                .maxLength("suburb", suburb, TEXT_MAX_LENGTH, "Suburb")
                .maxLength("city", city, TEXT_MAX_LENGTH, "City")
                .numeric("postCode", postCode, "Post code")
                .maxLength("postCode", postCode, TEXT_MAX_LENGTH, "Post code")
                .required("country", country, "Country")
                .maxLength("country", country, TEXT_MAX_LENGTH, "Country")
                .errors();
        }
    }
}
