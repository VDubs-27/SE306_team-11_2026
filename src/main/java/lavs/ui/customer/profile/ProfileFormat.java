package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formatting shared by the profile screen and its tabs. */
final class ProfileFormat {

    static final String NOT_RECORDED = "—";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
}
