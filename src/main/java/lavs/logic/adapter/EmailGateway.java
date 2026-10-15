package lavs.logic.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Email;
import mainframe.client.messages.customer.AddEmail;
import mainframe.client.messages.customer.DeleteEmail;
import mainframe.client.messages.customer.UpdateEmail;
import mainframe.client.messages.customerEmail.Retrieve;

public class EmailGateway {

    private final MainframeAdapter adapter;

    public EmailGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Email.Summary> list(CustomerNumber customer, long page, long size) {
        mainframe.client.messages.customerEmail.List message =
                adapter.send(new mainframe.client.messages.customerEmail.List(customer.value(), page, size));
        return new Page<>(summaries(customer, Arrays.asList(message.getItems())), page, size, message.getTotal());
    }

    public List<Email.Summary> all(CustomerNumber customer) {
        return summaries(customer, Pages.readAll(adapter,
                page -> new mainframe.client.messages.customerEmail.List(customer.value(), page, Pages.SIZE),
                mainframe.client.messages.customerEmail.List::getItems,
                mainframe.client.messages.customerEmail.List::getTotal));
    }

    public long count(CustomerNumber customer) {
        return list(customer, 1, 1).total();
    }

    public Email retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Email(
                message.getId(), customer, Email.Type.fromCode(message.getType()), message.getOtherType(),
                message.getAddress(), message.getIsPrimary());
    }

    public Email add(CustomerNumber customer, Email.Details details) {
        AddEmail message = adapter.send(new AddEmail(
                details.address(), customer.value(), details.isPrimary(), details.otherType(), details.type().code()));
        return email(message.getId(), customer, details);
    }

    public Email update(CustomerNumber customer, long id, Email.Details details) {
        adapter.send(new UpdateEmail(
                details.address(), customer.value(), id, details.isPrimary(), details.otherType(),
                details.type().code()));
        return email(id, customer, details);
    }

    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteEmail(customer.value(), id));
    }

    private static List<Email.Summary> summaries(
            CustomerNumber customer, List<mainframe.client.messages.customerEmail.List.Item> items) {
        return items.stream()
                .map(item -> new Email.Summary(
                        item.getId(), customer, Email.Type.fromCode(item.getType()), item.getAddress(),
                        Values.isYes(item.getIsPrimary())))
                .toList();
    }

    private static Email email(long id, CustomerNumber customer, Email.Details details) {
        return new Email(id, customer, details.type(), details.otherType(), details.address(), details.isPrimary());
    }
}
