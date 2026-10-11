package lavs.model.customer;

import java.util.Objects;

public record PhoneSummary(
    long id,
    CustomerNumber customerNumber,
    PhoneType type,
    String countryCode,
    String number,
    boolean isPrimary,
    boolean canTxt
) {
    public PhoneSummary {
        Objects.requireNonNull(customerNumber, "customerNumber");
        Objects.requireNonNull(type, "type");
    }
}
