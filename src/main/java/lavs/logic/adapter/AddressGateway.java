package lavs.logic.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.messages.customer.AddAddress;
import mainframe.client.messages.customer.DeleteAddress;
import mainframe.client.messages.customer.UpdateAddress;
import mainframe.client.messages.customerAddress.Retrieve;

public class AddressGateway {

    private final MainframeAdapter adapter;

    public AddressGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Address.Summary> list(CustomerNumber customer, long page, long size) {
        mainframe.client.messages.customerAddress.List message =
                adapter.send(new mainframe.client.messages.customerAddress.List(customer.value(), page, size));
        return new Page<>(summaries(customer, Arrays.asList(message.getItems())), page, size, message.getTotal());
    }

    public List<Address.Summary> all(CustomerNumber customer) {
        return summaries(customer, Pages.readAll(adapter,
                page -> new mainframe.client.messages.customerAddress.List(customer.value(), page, Pages.SIZE),
                mainframe.client.messages.customerAddress.List::getItems,
                mainframe.client.messages.customerAddress.List::getTotal));
    }

    public Address retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Address(
                message.getId(), customer, Address.Type.fromCode(message.getType()), message.getOtherType(),
                message.getLine1(), message.getLine2(), message.getSuburb(), message.getCity(),
                message.getPostCode(), message.getCountry(), message.getIsPrimary(), message.getIsMailing());
    }

    public Address add(CustomerNumber customer, Address.Details details) {
        AddAddress message = adapter.send(new AddAddress(
                details.city(), details.country(), customer.value(), details.isMailing(), details.isPrimary(),
                details.line1(), details.line2(), details.otherType(), details.postCode(), details.suburb(),
                details.type().code()));
        return address(message.getId(), customer, details);
    }

    public Address update(CustomerNumber customer, long id, Address.Details details) {
        adapter.send(new UpdateAddress(
                details.city(), details.country(), customer.value(), id, details.isMailing(), details.isPrimary(),
                details.line1(), details.line2(), details.otherType(), details.postCode(), details.suburb(),
                details.type().code()));
        return address(id, customer, details);
    }

    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteAddress(customer.value(), id));
    }

    private static List<Address.Summary> summaries(
            CustomerNumber customer, List<mainframe.client.messages.customerAddress.List.Item> items) {
        return items.stream()
                .map(item -> new Address.Summary(
                        item.getId(), customer, Address.Type.fromCode(item.getType()), item.getCity(),
                        item.getCountry(), Values.isYes(item.getIsPrimary()), Values.isYes(item.getIsMailing())))
                .toList();
    }

    private static Address address(long id, CustomerNumber customer, Address.Details details) {
        return new Address(
                id, customer, details.type(), details.otherType(), details.line1(), details.line2(),
                details.suburb(), details.city(), details.postCode(), details.country(), details.isPrimary(),
                details.isMailing());
    }
}
