package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.loan.LoanNumber;
import mainframe.client.messages.loan.AddDocument;
import mainframe.client.messages.loan.DeleteDocument;
import mainframe.client.messages.loanDocument.List;
import mainframe.client.messages.loanDocument.Retrieve;

final class LoanDocumentMessages implements DocumentMessages<LoanNumber> {

    private final MainframeAdapter adapter;

    LoanDocumentMessages(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public Listed list(LoanNumber loan, long page, long size) {
        List message = adapter.send(new List(loan.value(), page, size));
        return new Listed(
                Arrays.stream(message.getItems())
                        .map(item -> new Item(item.getId(), item.getType(), item.getFileName()))
                        .toList(),
                message.getTotal());
    }

    @Override
    public Fields retrieve(LoanNumber loan, long id) {
        Retrieve message = adapter.send(new Retrieve(id, loan.value()));
        return new Fields(
                message.getId(), message.getType(), message.getFileName(), message.getFileType(), message.getSize());
    }

    @Override
    public long add(LoanNumber loan, String type, String fileName, String fileType, long size) {
        return adapter.send(new AddDocument(fileName, fileType, loan.value(), size, type)).getId();
    }

    @Override
    public void delete(LoanNumber loan, long id) {
        adapter.send(new DeleteDocument(id, loan.value()));
    }
}
