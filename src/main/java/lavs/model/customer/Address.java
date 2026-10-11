package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            customerNumber, type, otherType, line1, line2, suburb, city, postCode, country, isPrimary, isMailing);
    }

    public enum Type {
        RESIDENTIAL,
        POSTAL,
        FORWARDING,
        DELEGATED,
        OTHER
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
        public Details {
            Objects.requireNonNull(customerNumber, "customerNumber");
        }

        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (type == null) {
                errors.add("Address type is required.");
            } else if (type == Type.OTHER && isBlank(otherType)) {
                errors.add("Other address type is required.");
            }

            if (isBlank(line1)) {
                errors.add("Address line 1 is required.");
            }

            return errors;
        }
    }
}
