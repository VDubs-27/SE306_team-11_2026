package lavs.ui.customer.profile;

/**
 * One document in the Documents tab.
 * Display-only, filled with sample data until there is a Document model in lavs.logic.
 */
public record DocumentRow(long id, String type, String fileName, String fileType, long size) {
}
