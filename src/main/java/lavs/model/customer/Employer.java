package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Employer(
    long id,
    CustomerNumber customerNumber,
    Type type,
    String name,
    String position,
    BigDecimal salary
) {
    public Employer {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public Details details() {
        return new Details(customerNumber, type, name, position, salary);
    }

    public enum Type {
        PRIMARY,
        SECONDARY
    }

    public record Summary(
        long id,
        CustomerNumber customerNumber,
        Type type,
        String name
    ) {
        public Summary {
            Objects.requireNonNull(customerNumber, "customerNumber");
            Objects.requireNonNull(type, "type");
        }
    }

    public record Details(
        CustomerNumber customerNumber,
        Type type,
        String name,
        String position,
        BigDecimal salary
    ) {
        public Details {
            Objects.requireNonNull(customerNumber, "customerNumber");
        }

        public List<String> validate() {
            List<String> errors = new ArrayList<>();

            if (type == null) {
                errors.add("Employer type is required.");
            }

            if (isBlank(name)) {
                errors.add("Employer name is required.");
            }

            return errors;
        }
    }
}
