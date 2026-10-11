package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EmployerDetails(
    CustomerNumber customerNumber,
    EmployerType type,
    String name,
    String position,
    BigDecimal salary
) {
    public EmployerDetails {
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
