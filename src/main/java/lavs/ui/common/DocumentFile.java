package lavs.ui.common;

/**
 * The details recorded about a document: what it is, and the chosen file's name, type and size.
 * Only these are saved; the file itself stays with the customer's paperwork.
 * Display-only until there is a Document model in lavs.logic.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record DocumentFile(String type, String fileName, String fileType, long size) {
}
