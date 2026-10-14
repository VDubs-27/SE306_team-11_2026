package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatting shared by the profile screen and its tabs. */
final class ProfileFormat {

    static final String NOT_RECORDED = "—";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

    private static final long KILOBYTE = 1024;

    private ProfileFormat() {
    }

    /** A date as dd/MM/yyyy, or a dash when there isn't one. */
    static String date(LocalDate date) {
        return date == null ? NOT_RECORDED : DATE_FORMAT.format(date);
    }

    /** The value, or a dash when it is blank. */
    static String orNotRecorded(String value) {
        return isBlank(value) ? NOT_RECORDED : value;
    }

    /** e.g. "$98,000", or "$1,234.50" when there are cents; a dash when there's no amount. */
    static String money(BigDecimal amount) {
        if (amount == null) {
            return NOT_RECORDED;
        }
        String pattern = amount.stripTrailingZeros().scale() <= 0 ? "$#,##0" : "$#,##0.00";
        return new DecimalFormat(pattern, SYMBOLS).format(amount);
    }

    /** e.g. "184 KB" or "1.2 MB". */
    static String fileSize(long bytes) {
        if (bytes < KILOBYTE) {
            return bytes + " B";
        }
        if (bytes < KILOBYTE * KILOBYTE) {
            return Math.round(bytes / (double) KILOBYTE) + " KB";
        }
        return new DecimalFormat("0.#", SYMBOLS).format(bytes / (double) (KILOBYTE * KILOBYTE)) + " MB";
    }
}
