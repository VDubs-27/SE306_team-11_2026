package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return new Details(customerNumber, type, otherType, countryCode, number, isPrimary, canTxt);
    }

    public enum Type {
        HOME,
        MOBILE,
        WORK,
        OTHER
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
        CustomerNumber customerNumber,
        Type type,
        String otherType,
        String countryCode,
        String number,
        boolean isPrimary,
        boolean canTxt
    ) {
        public Details {
            Objects.requireNonNull(customerNumber, "customerNumber");
        }

        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (type == null) {
                errors.add("Phone type is required.");
            } else if (type == Type.OTHER && isBlank(otherType)) {
                errors.add("Other phone type is required.");
            }

            if (isBlank(number)) {
                errors.add("Phone number is required.");
            }

            return errors;
        }
    }
}
