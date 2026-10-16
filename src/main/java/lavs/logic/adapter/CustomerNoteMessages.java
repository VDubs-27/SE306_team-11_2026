package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.messages.customer.AddNote;
import mainframe.client.messages.customer.DeleteNote;
import mainframe.client.messages.customer.UpdateNote;
import mainframe.client.messages.customerNote.List;
import mainframe.client.messages.customerNote.Retrieve;

final class CustomerNoteMessages implements NoteMessages<CustomerNumber> {

    private final MainframeAdapter adapter;

    CustomerNoteMessages(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    @Override
    public Listed list(CustomerNumber customer, long page, long size) {
        List message = adapter.send(new List(customer.value(), page, size));
        return new Listed(Arrays.stream(message.getItems()).map(List.Item::getId).toList(), message.getTotal());
    }

    @Override
    public Fields retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Fields(message.getType(), message.getLines(), message.getAuthor(), message.getDateAdded());
    }

    @Override
    public long add(CustomerNumber customer, String type, String lines) {
        return adapter.send(new AddNote(customer.value(), lines, type)).getId();
    }

    @Override
    public void update(CustomerNumber customer, long id, String type, String lines) {
        adapter.send(new UpdateNote(customer.value(), id, lines, type));
    }

    @Override
    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteNote(customer.value(), id));
    }
}
