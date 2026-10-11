package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return new Details(customerNumber, type, otherType, address, isPrimary);
    }

    public enum Type {
        PERSONAL,
        WORK,
        OTHER
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
        CustomerNumber customerNumber,
        Type type,
        String otherType,
        String address,
        boolean isPrimary
    ) {
        public Details {
            Objects.requireNonNull(customerNumber, "customerNumber");
        }

        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (type == null) {
                errors.add("Email type is required.");
            } else if (type == Type.OTHER && isBlank(otherType)) {
                errors.add("Other email type is required.");
            }

            if (isBlank(address)) {
                errors.add("Email address is required.");
            }

            return errors;
        }
    }
}
