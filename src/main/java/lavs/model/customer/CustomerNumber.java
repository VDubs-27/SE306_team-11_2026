package lavs.model.customer;

public record CustomerNumber(String value) {
    public CustomerNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Customer number must not be null or blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}