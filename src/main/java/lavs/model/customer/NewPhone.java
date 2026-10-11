package lavs.model.customer;

import static lavs.model.Validation.isBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record NewPhone(
    CustomerNumber customerNumber,
    PhoneType type,
    String otherType,
    String countryCode,
    String number,
    boolean isPrimary,
    boolean canTxt
) {
    public NewPhone {
        Objects.requireNonNull(customerNumber, "customerNumber");
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Phone type is required.");
        } else if (type == PhoneType.OTHER && isBlank(otherType)) {
            errors.add("Other phone type is required.");
        }

        if (isBlank(number)) {
            errors.add("Phone number is required.");
        }

        return errors;
    }
}
