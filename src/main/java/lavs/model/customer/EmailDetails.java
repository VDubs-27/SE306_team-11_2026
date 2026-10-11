package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EmailDetails(
    CustomerNumber customerNumber,
    EmailType type,
    String otherType,
    String address,
    boolean isPrimary
) {
    public EmailDetails {
        Objects.requireNonNull(customerNumber, "customerNumber");
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Email type is required.");
        } else if (type == EmailType.OTHER && isBlank(otherType)) {
            errors.add("Other email type is required.");
        }

        if (isBlank(address)) {
            errors.add("Email address is required.");
        }

        return errors;
    }
}
