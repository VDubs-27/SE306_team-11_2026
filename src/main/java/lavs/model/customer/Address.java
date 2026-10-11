package lavs.model.customer;

import java.util.Objects;

public record Address(
    long id,
    CustomerNumber customerNumber,
    AddressType type,
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

    public AddressDetails details() {
        return new AddressDetails(
            customerNumber, type, otherType, line1, line2, suburb, city, postCode, country, isPrimary, isMailing);
    }
}
