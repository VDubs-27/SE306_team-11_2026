package lavs.model.customer;

import java.util.Objects;

public record Email(
    long id,
    CustomerNumber customerNumber,
    EmailType type,
    String otherType,
    String address,
    boolean isPrimary
) {
    public Email {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
