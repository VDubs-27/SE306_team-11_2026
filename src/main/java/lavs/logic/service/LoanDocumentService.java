package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import lavs.logic.adapter.LoanDocumentGateway;
import lavs.logic.adapter.MainframeException;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.loan.LoanDocument;
import lavs.logic.model.loan.LoanNumber;

public class LoanDocumentService {

    static final String ORIGINAL_NOT_REMOVED_MESSAGE =
            "The changes were saved as a new document, but the original could not be removed. Delete the older copy.";

    private final LoanDocumentGateway documents;

    public LoanDocumentService(LoanDocumentGateway documents) {
        this.documents = Objects.requireNonNull(documents, "documents");
    }

    public Result<Page<LoanDocument.Summary>> list(LoanNumber loan, long page, long size) {
        return Result.attempt(() -> Result.success(documents.list(loan, page, size)));
    }

    public Result<LoanDocument> get(LoanNumber loan, long id) {
        return Result.attempt(() -> Result.success(documents.retrieve(loan, id)));
    }

    public Result<LoanDocument> add(LoanNumber loan, LoanDocument.Details details) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> Result.success(documents.add(loan, details)));
    }

    // The mainframe has no update message for documents, so an update adds the new details as a replacement and
    // then deletes the original (deviation 12). Adding first means a failure leaves a duplicate, never a lost document.
    public Result<LoanDocument> update(LoanNumber loan, long id, LoanDocument.Details details) {
        List<FieldError> errors = details.validate();
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            LoanDocument original = documents.retrieve(loan, id);
            if (original.details().equals(details)) {
                return Result.success(original);
            }
            LoanDocument replacement = documents.add(loan, details);
            try {
                documents.delete(loan, id);
            } catch (MainframeException e) {
                return new Result<>(replacement, List.of(), ORIGINAL_NOT_REMOVED_MESSAGE);
            }
            return Result.success(replacement);
        });
    }

    public Result<Void> delete(LoanNumber loan, long id) {
        return Result.attempt(() -> {
            documents.delete(loan, id);
            return Result.success(null);
        });
    }
}
