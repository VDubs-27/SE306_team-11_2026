package lavs.ui.common;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.Locale;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * How a document is shown as a row, for a customer or a loan: a file icon, the file name,
 * "PDF · 184 KB", its type as a badge and an Edit button.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public final class DocumentRows {

    private DocumentRows() {
    }

    public static HBox tile(String type, String fileName, String fileType, long size, Runnable onEdit) {
        return RecordTiles.iconTile(fileIcon(), fileName, details(fileName, fileType, size),
                isBlank(type) ? List.of() : List.of(RecordTiles.badge(type, "badge-mailing")), onEdit);
    }

    /** e.g. "PDF · 184 KB". */
    public static String details(String fileName, String fileType, long size) {
        return format(fileName, fileType) + " · " + DisplayFormat.fileSize(size);
    }

    /** The file's extension in capitals, or the end of its file type when the name has none. */
    public static String format(String fileName, String fileType) {
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toUpperCase(Locale.ROOT);
        }
        String type = DisplayFormat.orNotRecorded(fileType);
        return type.substring(type.indexOf('/') + 1).toUpperCase(Locale.ROOT);
    }

    /** A page outline with a folded corner, on a small tinted square. */
    private static StackPane fileIcon() {
        SVGPath page = new SVGPath();
        page.setContent("M2 1h7l4 4v10H2z M9 1v4h4");
        page.getStyleClass().add("file-icon-glyph");
        StackPane icon = new StackPane(page);
        icon.getStyleClass().add("file-icon");
        return icon;
    }
}
