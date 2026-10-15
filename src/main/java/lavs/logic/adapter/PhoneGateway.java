package lavs.logic.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Phone;
import mainframe.client.messages.customer.AddPhoneNumber;
import mainframe.client.messages.customer.DeletePhoneNumber;
import mainframe.client.messages.customer.UpdatePhoneNumber;
import mainframe.client.messages.customerPhoneNumber.Retrieve;

public class PhoneGateway {

    private final MainframeAdapter adapter;

    public PhoneGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Phone.Summary> list(CustomerNumber customer, long page, long size) {
        PhoneNumberList message = adapter.send(new PhoneNumberList(customer.value(), page, size));
        return new Page<>(summaries(customer, Arrays.asList(message.getItems())), page, size, message.getTotal());
    }

    public List<Phone.Summary> all(CustomerNumber customer) {
        return summaries(customer, Pages.readAll(adapter,
                page -> new PhoneNumberList(customer.value(), page, Pages.SIZE),
                PhoneNumberList::getItems,
                PhoneNumberList::getTotal));
    }

    public long count(CustomerNumber customer) {
        return list(customer, 1, 1).total();
    }

    public Phone retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Phone(
                message.getId(), customer, Phone.Type.fromCode(message.getType()), message.getOtherType(),
                message.getCountryCode(), message.getNumber(), message.getIsPrimary(), message.getCanTxt());
    }

    public Phone add(CustomerNumber customer, Phone.Details details) {
        AddPhoneNumber message = adapter.send(new AddPhoneNumber(
                details.canTxt(), details.countryCode(), customer.value(), details.isPrimary(), details.number(),
                details.otherType(), details.type().code()));
        return phone(message.getId(), customer, details);
    }

    public Phone update(CustomerNumber customer, long id, Phone.Details details) {
        adapter.send(new UpdatePhoneNumber(
                details.canTxt(), details.countryCode(), customer.value(), id, details.isPrimary(), details.number(),
                details.otherType(), details.type().code()));
        return phone(id, customer, details);
    }

    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeletePhoneNumber(customer.value(), id));
    }

    private static List<Phone.Summary> summaries(CustomerNumber customer, List<PhoneNumberList.Item> items) {
        return items.stream()
                .map(item -> new Phone.Summary(
                        item.id(), customer, Phone.Type.fromCode(item.type()), item.countryCode(),
                        item.number(), Values.isYes(item.isPrimary()), Values.isYes(item.canTxt())))
                .toList();
    }

    private static Phone phone(long id, CustomerNumber customer, Phone.Details details) {
        return new Phone(
                id, customer, details.type(), details.otherType(), details.countryCode(), details.number(),
                details.isPrimary(), details.canTxt());
    }
}
