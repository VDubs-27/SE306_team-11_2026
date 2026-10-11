package lavs.model;

public final class Validation {

    private Validation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
