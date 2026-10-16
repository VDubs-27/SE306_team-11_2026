package lavs.ui.common;

import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A Documents tab, on a customer's profile or a loan: the files recorded, a page at a time from the
 * mainframe, which can be added, changed or removed there.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    private ServiceList<DocumentFile, DocumentFile> documents;

    @FXML
    private void initialize() {
        documents = new ServiceList<>(
                new RecordEditor<>(documentList, "document", documentForm, documentFormController),
                documentList, pagerController, PAGE_SIZE, DocumentRows::tile, document -> document,
                "No documents recorded.");
    }

    /** Shows the documents kept on the mainframe, e.g. a customer's, and saves changes there; onTotal hears the count. */
    public void show(ServiceList.Source<DocumentFile, DocumentFile> source, LongConsumer onTotal) {
        documents.onTotal(onTotal).show(source);
    }

    @FXML
    private void handleAddDocument() {
        documents.add();
    }
}
