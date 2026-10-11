package lavs.model.customer;

import java.util.Objects;

public record Phone(
    long id,
    CustomerNumber customerNumber,
    PhoneType type,
    String otherType,
    String countryCode,
    String number,
    boolean isPrimary,
    boolean canTxt
) {
    public Phone {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }

    public PhoneDetails details() {
        return new PhoneDetails(customerNumber, type, otherType, countryCode, number, isPrimary, canTxt);
    }
}
