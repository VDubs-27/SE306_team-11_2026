package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import lavs.logic.adapter.DocumentGateway;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.document.Document;

// Documents on a customer or a loan; K is the owner's number,
// e.g. new DocumentService<>(DocumentGateway.forLoans(adapter))
public class DocumentService<K> {

    static final String ORIGINAL_NOT_REMOVED_MESSAGE =
            "The changes were saved as a new document, but the original could not be removed. Delete the older copy.";

    private final DocumentGateway<K> documents;

    public DocumentService(DocumentGateway<K> documents) {
        this.documents = Objects.requireNonNull(documents, "documents");
    }

    public Result<Page<Document.Summary>> list(K owner, long page, long size) {
        return Result.attempt(() -> Result.success(documents.list(owner, page, size)));
    }

    public Result<Document> get(K owner, long id) {
        return Result.attempt(() -> Result.success(documents.retrieve(owner, id)));
    }

    public Result<Document> add(K owner, Document.Details details) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(documents.add(owner, details)));
    }

    // The mainframe has no update message for documents, so an update adds the new details as a replacement and
    // then deletes the original (deviation 12). Adding first means a failure leaves a duplicate, never a lost document.
    public Result<Document> update(K owner, long id, Document.Details details) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            Document original = documents.retrieve(owner, id);
            if (original.details().equals(details)) {
                return Result.success(original);
            }
            Document replacement = documents.add(owner, details);
            try {
                documents.delete(owner, id);
            } catch (MainframeException e) {
                return new Result<>(replacement, List.of(), ORIGINAL_NOT_REMOVED_MESSAGE);
            }
            return Result.success(replacement);
        });
    }

    public Result<Void> delete(K owner, long id) {
        return Result.attempt(() -> {
            documents.delete(owner, id);
            return Result.success(null);
        });
    }
}
