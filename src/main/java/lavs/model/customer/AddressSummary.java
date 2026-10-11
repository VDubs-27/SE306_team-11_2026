package lavs.model.customer;

import java.util.Objects;

public record AddressSummary(
    long id,
    CustomerNumber customerNumber,
    AddressType type,
    String city,
    String country,
    boolean isPrimary,
    boolean isMailing
) {
    public AddressSummary {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
