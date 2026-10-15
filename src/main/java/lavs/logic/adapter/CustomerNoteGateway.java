package lavs.logic.adapter;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNote;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.StatusMessage;
import mainframe.client.messages.customer.AddNote;
import mainframe.client.messages.customer.DeleteNote;
import mainframe.client.messages.customer.UpdateNote;
import mainframe.client.messages.customerNote.List;
import mainframe.client.messages.customerNote.Retrieve;

public class CustomerNoteGateway {

    private static final String UNREADABLE_TEXT = "The mainframe returned a note that could not be read.";

    private final MainframeAdapter adapter;

    public CustomerNoteGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    // The list message has no note text, so each note on the page is fetched for its text
    public Page<CustomerNote> list(CustomerNumber customer, long page, long size) {
        List message = adapter.send(new List(customer.value(), page, size));
        return new Page<>(
                Arrays.stream(message.getItems())
                        .map(item -> readable(customer, item.getId()))
                        .flatMap(Optional::stream)
                        .toList(),
                page, size, message.getTotal());
    }

    public long count(CustomerNumber customer) {
        return adapter.send(new List(customer.value(), 1, 1)).getTotal();
    }

    public CustomerNote retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        try {
            return new CustomerNote(
                    id, customer, CustomerNote.Type.fromCode(message.getType()), message.getLines(),
                    message.getAuthor(), Values.date(message.getDateAdded()));
        } catch (DateTimeParseException e) {
            throw new MainframeException(StatusMessage.UNABLE_TO_PARSE.getCode(), UNREADABLE_TEXT, e);
        }
    }

    public CustomerNote add(CustomerNumber customer, CustomerNote.Details details) {
        AddNote message = adapter.send(new AddNote(customer.value(), details.lines(), details.type().code()));
        return new CustomerNote(message.getId(), customer, details.type(), details.lines(), message.getAuthor(), null);
    }

    public CustomerNote update(CustomerNumber customer, long id, CustomerNote.Details details) {
        UpdateNote message = adapter.send(
                new UpdateNote(customer.value(), id, details.lines(), details.type().code()));
        return new CustomerNote(id, customer, details.type(), details.lines(), message.getAuthor(), null);
    }

    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteNote(customer.value(), id));
    }

    private Optional<CustomerNote> readable(CustomerNumber customer, long id) {
        try {
            return Optional.of(retrieve(customer, id));
        } catch (MainframeException e) {
            int unreadable = StatusMessage.UNABLE_TO_PARSE.getCode();
            if (e.errorCode() == unreadable) {
                return Optional.empty();
            }
            throw e;
        }
    }
}