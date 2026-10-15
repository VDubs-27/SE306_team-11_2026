package lavs.logic.adapter;

import java.util.Arrays;
import java.util.Objects;
import lavs.logic.model.Page;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Employer;
import mainframe.client.messages.customer.AddEmployer;
import mainframe.client.messages.customer.DeleteEmployer;
import mainframe.client.messages.customer.UpdateEmployer;
import mainframe.client.messages.customerEmployer.List;
import mainframe.client.messages.customerEmployer.Retrieve;

public class EmployerGateway {

    private final MainframeAdapter adapter;

    public EmployerGateway(MainframeAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter, "adapter");
    }

    public Page<Employer.Summary> list(CustomerNumber customer, long page, long size) {
        List message = adapter.send(new List(customer.value(), page, size));
        return new Page<>(
                Arrays.stream(message.getItems())
                        .map(item -> new Employer.Summary(
                                item.getId(), customer, Employer.Type.fromCode(item.getType()), item.getName()))
                        .toList(),
                page, size, message.getTotal());
    }

    public java.util.List<Employer.Summary> all(CustomerNumber customer) {
        return Pages.readAll(adapter,
                        page -> new List(customer.value(), page, Pages.SIZE),
                        List::getItems,
                        List::getTotal)
                .stream()
                .map(item -> new Employer.Summary(
                        item.getId(), customer, Employer.Type.fromCode(item.getType()), item.getName()))
                .toList();
    }

    public long count(CustomerNumber customer) {
        return list(customer, 1, 1).total();
    }

    public Employer retrieve(CustomerNumber customer, long id) {
        Retrieve message = adapter.send(new Retrieve(id, customer.value()));
        return new Employer(
                message.getId(), customer, Employer.Type.fromCode(message.getType()), message.getName(),
                message.getPosition(), Values.decimal(message.getSalary()));
    }

    public Employer add(CustomerNumber customer, Employer.Details details) {
        AddEmployer message = adapter.send(new AddEmployer(
                customer.value(), details.name(), details.position(), Values.toDouble(details.salary()),
                details.type().code()));
        return employer(message.getId(), customer, details);
    }

    public Employer update(CustomerNumber customer, long id, Employer.Details details) {
        adapter.send(new UpdateEmployer(
                customer.value(), id, details.name(), details.position(), Values.toDouble(details.salary()),
                details.type().code()));
        return employer(id, customer, details);
    }

    public void delete(CustomerNumber customer, long id) {
        adapter.send(new DeleteEmployer(customer.value(), id));
    }

    private static Employer employer(long id, CustomerNumber customer, Employer.Details details) {
        return new Employer(id, customer, details.type(), details.name(), details.position(), details.salary());
    }
}
