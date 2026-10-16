package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A Documents tab, on a customer's profile or a loan: the files recorded, a page at a time, which
 * can be added, changed or removed. The screen it's on gives it the documents, either kept on screen
 * (use) or on the mainframe (show), and hears about changes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    private static final String EMPTY = "No documents recorded.";

    private EditableList<DocumentFile> documents;
    private ServiceList<DocumentFile, DocumentFile> saved;

    @FXML
    private void initialize() {
        RecordEditor<DocumentFile> editor =
                new RecordEditor<>(documentList, "document", documentForm, documentFormController);
        documents = new EditableList<>(editor, documentList, DocumentRows::tile, EMPTY)
                .paged(pagerController, PAGE_SIZE);
        saved = new ServiceList<>(editor, documentList, pagerController, PAGE_SIZE, DocumentRows::tile,
                document -> document, EMPTY);
    }

    /** What happens after a document is added, changed or removed, e.g. the screen updates the tab's count. */
    public void setOnChanged(Runnable onChanged) {
        documents.onChanged(onChanged);
    }

    /** Shows the documents kept on the mainframe, e.g. a customer's, and saves changes there; onTotal hears the count. */
    public void show(ServiceList.Source<DocumentFile, DocumentFile> source, LongConsumer onTotal) {
        saved.onTotal(onTotal).show(source);
        documents = null;
    }

    /** Shows these documents from the first page. */
    public void use(List<DocumentFile> recorded) {
        documents.use(new ArrayList<>(recorded));
    }

    /** How many documents there are, for the tab's count. */
    public long count() {
        return documents.items().size();
    }

    @FXML
    private void handleAddDocument() {
        if (documents == null) {
            saved.add();
        } else {
            documents.add();
        }
    }
}
