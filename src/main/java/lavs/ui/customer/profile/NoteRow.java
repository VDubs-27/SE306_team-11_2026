package lavs.ui.customer.profile;

import java.time.LocalDate;

/**
 * One note in the Notes tab.
 * Display-only, filled with sample data until there is a Note model in lavs.logic.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record NoteRow(long id, String type, String author, LocalDate dateAdded, String text) {
}
