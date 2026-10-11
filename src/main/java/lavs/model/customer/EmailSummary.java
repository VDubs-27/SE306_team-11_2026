package lavs.model.customer;

import java.util.Objects;

public record EmailSummary(
    long id,
    CustomerNumber customerNumber,
    EmailType type,
    String address,
    boolean isPrimary
) {
    public EmailSummary {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
