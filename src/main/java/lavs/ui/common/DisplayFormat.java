package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import lavs.logic.model.customer.Customer;

/**
 * How values are written on screen: dates, money, file sizes and missing values.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class DisplayFormat {

    public static final String NOT_RECORDED = "—";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

    private static final long KILOBYTE = 1024;

    private DisplayFormat() {
    }

    /** A date as dd/MM/yyyy, or a dash when there isn't one. */
    public static String date(LocalDate date) {
        return date == null ? NOT_RECORDED : DATE_FORMAT.format(date);
    }

    /** The value, or a dash when it is blank. */
    public static String orNotRecorded(String value) {
        return isBlank(value) ? NOT_RECORDED : value;
    }

    /** e.g. "$98,000", or "$1,234.50" when there are cents; a dash when there's no amount. */
    public static String money(BigDecimal amount) {
        if (amount == null) {
            return NOT_RECORDED;
        }
        String pattern = amount.stripTrailingZeros().scale() <= 0 ? "$#,##0" : "$#,##0.00";
        return new DecimalFormat(pattern, SYMBOLS).format(amount);
    }

    /** Always with cents, e.g. "$1,050.00", for amounts like repayments; a dash when there's no amount. */
    public static String moneyWithCents(BigDecimal amount) {
        return amount == null ? NOT_RECORDED : new DecimalFormat("$#,##0.00", SYMBOLS).format(amount);
    }

    /** e.g. "184 KB" or "1.2 MB". */
    public static String fileSize(long bytes) {
        if (bytes < KILOBYTE) {
            return bytes + " B";
        }
        if (bytes < KILOBYTE * KILOBYTE) {
            return Math.round(bytes / (double) KILOBYTE) + " KB";
        }
        return new DecimalFormat("0.#", SYMBOLS).format(bytes / (double) (KILOBYTE * KILOBYTE)) + " MB";
    }

    /** A customer's initials for an avatar, e.g. "JS" for John Smith. */
    public static String initials(Customer customer) {
        return customer.firstName().substring(0, 1) + customer.familyName().substring(0, 1);
    }

    /** A sign-in name as a person's name, e.g. "bob.ross" as "Bob Ross". */
    public static String userName(String userName) {
        return String.join(" ", userNameParts(userName));
    }

    /** Initials for a sign-in name, e.g. "bob.ross" as "BR", or "admin" as "A". */
    public static String userInitials(String userName) {
        StringBuilder initials = new StringBuilder();
        for (String part : userNameParts(userName)) {
            if (initials.length() < 2) {
                initials.append(part.charAt(0));
            }
        }
        return initials.toString();
    }

    /** The words in a sign-in name, split at dots, underscores, dashes and spaces, each capitalised. */
    private static List<String> userNameParts(String userName) {
        return Arrays.stream(userName.trim().split("[._\\s-]+"))
                .filter(part -> !part.isEmpty())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .toList();
    }

    /** e.g. "Mr John Smith", or just "John Smith" when there's no title. */
    public static String fullNameWithTitle(Customer customer) {
        return isBlank(customer.title()) ? customer.fullName() : customer.title() + " " + customer.fullName();
    }
}
