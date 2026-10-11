package lavs.model.customer;

import java.util.Objects;

public record EmployerSummary(
    long id,
    CustomerNumber customerNumber,
    EmployerType type,
    String name
) {
    public EmployerSummary {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
