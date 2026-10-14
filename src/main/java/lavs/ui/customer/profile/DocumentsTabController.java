package lavs.ui.customer.profile;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/** The Documents tab: the files held for the customer, such as proof of identity. */
public class DocumentsTabController {

    @FXML private VBox documentList;

    void show(List<DocumentRow> documents) {
        RecordTiles.fill(documentList, documents.stream()
                .map(document -> RecordTiles.tile(ProfileFormat.orNotRecorded(document.type()),
                        document.fileName(), null, List.of(), "View", () -> handleViewDocument(document)))
                .toList(), "No documents recorded.");
    }

    @FXML
    private void handleAddDocument() {
        // Later: open the add document form
    }

    private void handleViewDocument(DocumentRow document) {
        // Later: open the document details for this document
    }
}
