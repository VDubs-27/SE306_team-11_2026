package lavs.ui.common;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * A Documents tab, on a customer's profile or a loan: the files recorded, a page at a time, which
 * can be added, changed or removed. The screen it's on gives it the documents and hears about changes.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public class DocumentsTabController {

    private static final long PAGE_SIZE = 5;

    @FXML private Node documentForm;
    @FXML private DocumentFormController documentFormController;
    @FXML private VBox documentList;
    @FXML private PagerController pagerController;

    private EditableList<DocumentFile> documents;

    @FXML
    private void initialize() {
        documents = new EditableList<>(
                new RecordEditor<>(documentList, "document", documentForm, documentFormController),
                documentList, DocumentRows::tile, "No documents recorded.")
                .paged(pagerController, PAGE_SIZE);
    }

    /** What happens after a document is added, changed or removed, e.g. the screen updates the tab's count. */
    public void setOnChanged(Runnable onChanged) {
        documents.onChanged(onChanged);
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
        documents.add();
    }
}
