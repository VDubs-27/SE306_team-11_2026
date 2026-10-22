package lavs.ui.common;

import java.time.LocalDate;

/**
 * One note in a Notes tab, on a customer's profile or a loan.
 * Display-only, filled with sample data until there is a Note model in lavs.logic.
 * AI-assisted: written by Jack, refined with Claude.
 */
public record NoteRow(long id, String type, String author, LocalDate dateAdded, String text) {
}
