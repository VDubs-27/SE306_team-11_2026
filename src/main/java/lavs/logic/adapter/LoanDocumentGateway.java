package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.loan.LoanDocument;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.messages.loan.AddDocument;
import mainframe.client.messages.loan.DeleteDocument;
import mainframe.client.messages.loanDocument.List;
import mainframe.client.messages.loanDocument.Retrieve;

public class LoanDocumentGateway {

    private final MainframeAdapter adapter;

    public LoanDocumentGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<LoanDocument.Summary> list(LoanNumber loan, long page, long size) {
        List message = adapter.send(new List(loan.value(), page, size));
        return new Page<>(
                Arrays.stream(message.getItems())
                        .map(item -> new LoanDocument.Summary(item.getId(), loan, item.getType(), item.getFileName()))
                        .toList(),
                page, size, message.getTotal());
    }

    public LoanDocument retrieve(LoanNumber loan, long id) {
        Retrieve message = adapter.send(new Retrieve(id, loan.value()));
        return new LoanDocument(
                message.getId(), loan, message.getType(), message.getFileName(), message.getFileType(),
                message.getSize());
    }

    public LoanDocument add(LoanNumber loan, LoanDocument.Details details) {
        AddDocument message = adapter.send(new AddDocument(
                details.fileName(), details.fileType(), loan.value(), details.size(), details.type()));
        return new LoanDocument(
                message.getId(), loan, details.type(), details.fileName(), details.fileType(), details.size());
    }

    public void delete(LoanNumber loan, long id) {
        adapter.send(new DeleteDocument(id, loan.value()));
    }
}
