package lavs.model.customer;

import static lavs.model.Validation.isBlank;

public record CustomerNumber(String value) {
    public CustomerNumber {
        if (isBlank(value)) {
            throw new IllegalArgumentException("Customer number must not be null or blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}