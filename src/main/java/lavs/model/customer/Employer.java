package lavs.model.customer;

import java.math.BigDecimal;
import java.util.Objects;

public record Employer(
    long id,
    CustomerNumber customerNumber,
    EmployerType type,
    String name,
    String position,
    BigDecimal salary
) {
    public Employer {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
