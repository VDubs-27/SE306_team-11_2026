package lavs.ui.common;

import java.math.BigDecimal;

/**
 * Reads numbers typed into forms, allowing the way people write them: "$450,000", "6.29%".
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class Numbers {

    private Numbers() {
    }

    /** The number typed, ignoring "$", "%", commas and spaces; null when blank or not a number. */
    public static BigDecimal parse(String text) {
        String cleaned = text == null ? "" : text.replaceAll("[$%,\\s]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** True when something has been typed but it isn't a number. */
    public static boolean isInvalid(String text) {
        return text != null && !text.isBlank() && parse(text) == null;
    }
}
