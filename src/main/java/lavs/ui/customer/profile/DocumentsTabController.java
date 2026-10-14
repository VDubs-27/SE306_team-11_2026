package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import java.util.List;
import java.util.Locale;
import java.util.function.LongFunction;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import lavs.logic.model.Page;

/** The Documents tab: the files recorded for the customer, such as a payslip or licence, a page at a time. */
public class DocumentsTabController {

    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    private LongFunction<Page<DocumentRow>> loadPage = page -> new Page<>(List.of(), 1, 1, 0);

    @FXML
    private void initialize() {
        pagerController.setOnPageRequested(this::showPage);
    }

    /** Shows the first page and returns how many there are altogether, for the tab's count. */
    long show(LongFunction<Page<DocumentRow>> loadPage) {
        this.loadPage = loadPage;
        return showPage(1).total();
    }

    private Page<DocumentRow> showPage(long pageNumber) {
        Page<DocumentRow> page = loadPage.apply(pageNumber);
        RecordTiles.fill(documentList, page.items().stream()
                .map(document -> RecordTiles.iconTile(fileIcon(), document.fileName(), details(document),
                        badges(document), () -> handleEditDocument(document)))
                .toList(), "No documents recorded.");
        pagerController.show(page);
        return page;
    }

    @FXML
    private void handleAddDocument() {
        // Later: open the add document form
    }

    private void handleEditDocument(DocumentRow document) {
        // Later: open the document details for this document
    }

    /** e.g. "PDF · 184 KB". */
    private static String details(DocumentRow document) {
        return format(document) + " · " + ProfileFormat.fileSize(document.size());
    }

    /** The file's extension in capitals, or the end of its file type when the name has none. */
    private static String format(DocumentRow document) {
        String name = document.fileName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            return name.substring(dot + 1).toUpperCase(Locale.ROOT);
        }
        String fileType = ProfileFormat.orNotRecorded(document.fileType());
        return fileType.substring(fileType.indexOf('/') + 1).toUpperCase(Locale.ROOT);
    }

    private static List<Label> badges(DocumentRow document) {
        return isBlank(document.type())
                ? List.of()
                : List.of(RecordTiles.badge(document.type(), "badge-mailing"));
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
