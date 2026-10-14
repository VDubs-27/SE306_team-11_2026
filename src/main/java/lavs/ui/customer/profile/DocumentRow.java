package lavs.ui.customer.profile;

/**
 * One document in the Documents tab.
 * Display-only, filled with sample data until there is a Document model in lavs.logic.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record DocumentRow(long id, String type, String fileName, String fileType, long size) {
}
