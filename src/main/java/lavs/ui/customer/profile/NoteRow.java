package lavs.ui.customer.profile;

import java.time.LocalDate;

/**
 * One note in the Notes tab.
 * Display-only, filled with sample data until there is a Note model in lavs.logic.
 */
public record NoteRow(long id, String type, String author, LocalDate dateAdded) {
}
