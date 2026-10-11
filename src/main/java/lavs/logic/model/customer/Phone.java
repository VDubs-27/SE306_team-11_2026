package lavs.logic.model.customer;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.Objects;
import lavs.logic.model.Codes;
import lavs.logic.model.FieldError;
import lavs.logic.model.Validator;

public record Phone(
    long id,
    CustomerNumber customerNumber,
    Type type,
    String otherType,
    String countryCode,
    String number,
    boolean isPrimary,
    boolean canTxt
) {
    public Phone {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(type, otherType, countryCode, number, isPrimary, canTxt);
    }

    public enum Type {
        HOME("Home"),
        MOBILE("Mobile"),
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
        String countryCode,
        String number,
        boolean isPrimary,
        boolean canTxt
    ) {
        public Summary {
            Objects.requireNonNull(customerNumber, "customerNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        Type type,
        String otherType,
        String countryCode,
        String number,
        boolean isPrimary,
        boolean canTxt
    ) {
        public List<FieldError> validate() {
            return new Validator()
                .required("type", type, "Phone type")
                .check(type == Type.UNKNOWN, "type", "Phone type must be one of the listed options.")
                .check(type == Type.OTHER && isBlank(otherType), "otherType", "Other phone type is required.")
                .required("countryCode", countryCode, "Country code")
                .required("number", number, "Phone number")
                .errors();
        }
    }
}
