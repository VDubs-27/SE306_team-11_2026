package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AddressDetails(
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
    public AddressDetails {
        Objects.requireNonNull(customerNumber, "customerNumber");
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Address type is required.");
        } else if (type == AddressType.OTHER && isBlank(otherType)) {
            errors.add("Other address type is required.");
        }

        if (isBlank(line1)) {
            errors.add("Address line 1 is required.");
        }

        return errors;
    }
}
