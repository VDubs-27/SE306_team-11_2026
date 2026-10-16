package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.messages.customer.AddDocument;
import mainframe.client.messages.customer.DeleteDocument;
import mainframe.client.messages.customerDocument.List;
import mainframe.client.messages.customerDocument.Retrieve;

final class CustomerDocumentMessages implements DocumentMessages<CustomerNumber> {

    private final MainframeAdapter adapter;

    CustomerDocumentMessages(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public Listed list(CustomerNumber customer, long page, long size) {
        List message = adapter.send(new List(customer.value(), page, size));
        return new Listed(
                Arrays.stream(message.getItems())
                        .map(item -> new Item(item.getId(), item.getType(), item.getFileName()))
                        .toList(),
                message.getTotal());
    }

    @Override
    public Fields retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Fields(
                message.getId(), message.getType(), message.getFileName(), message.getFileType(), message.getSize());
    }

    @Override
    public long add(CustomerNumber customer, String type, String fileName, String fileType, long size) {
        return adapter.send(new AddDocument(customer.value(), fileName, fileType, size, type)).getId();
    }

    @Override
    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteDocument(customer.value(), id));
    }
}
