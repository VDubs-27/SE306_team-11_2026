package lavs.logic.adapter;

import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.document.Document;
import lavs.logic.model.loan.LoanNumber;

// Documents on a customer or a loan; K is the owner's number. forCustomers and forLoans pick the messages to send.
public final class DocumentGateway<K> {

    private final DocumentMessages<K> messages;

    private DocumentGateway(DocumentMessages<K> messages) {
        this.messages = messages;
    }

    public static DocumentGateway<CustomerNumber> forCustomers(MainframeAdapter adapter) {
        return new DocumentGateway<>(new CustomerDocumentMessages(adapter));
    }

    public static DocumentGateway<LoanNumber> forLoans(MainframeAdapter adapter) {
        return new DocumentGateway<>(new LoanDocumentMessages(adapter));
    }

    public Page<Document.Summary> list(K owner, long page, long size) {
        DocumentMessages.Listed listed = messages.list(owner, page, size);
        return new Page<>(
                listed.items().stream()
                        .map(item -> new Document.Summary(item.id(), item.type(), item.fileName()))
                        .toList(),
                page, size, listed.total());
    }

    public Document retrieve(K owner, long id) {
        DocumentMessages.Fields fields = messages.retrieve(owner, id);
        return new Document(fields.id(), fields.type(), fields.fileName(), fields.fileType(), fields.size());
    }

    public Document add(K owner, Document.Details details) {
        long id = messages.add(owner, details.type(), details.fileName(), details.fileType(), details.size());
        return new Document(id, details.type(), details.fileName(), details.fileType(), details.size());
    }

    public void delete(K owner, long id) {
        messages.delete(owner, id);
    }
}
