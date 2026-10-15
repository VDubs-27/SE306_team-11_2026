package lavs.logic.adapter;

import static lavs.logic.model.Validation.isBlank;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import mainframe.client.StatusMessage;
import mainframe.client.messages.customer.Create;
import mainframe.client.messages.customer.Retrieve;
import mainframe.client.messages.customer.Search;
import mainframe.client.messages.customer.Update;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class CustomerGateway {

    private final MainframeAdapter adapter;

    public CustomerGateway(MainframeAdapter adapter) {
        this.adapter = adapter;
    }

    public Page<Customer.Summary> search(long page, String searchTerm, long size) {
        Search search = adapter.send(new Search(page, searchTerm, size));
        List<Customer.Summary> summaries = new ArrayList<>();
        for (Search.Item item : search.getItems()) {
            summaries.add(toSummary(item));
        }
        return new Page<>(summaries, page, size, search.getTotal());
    }

    public Customer retrieve(CustomerNumber customerNumber) {
        Retrieve retrieve = adapter.send(new Retrieve(customerNumber.value()));
        return toCustomer(customerNumber, retrieve.getTitle(), retrieve.getFirstName(), retrieve.getFamilyName(),
                retrieve.getStatus(), retrieve.getDateOfBirth(), retrieve.getNationality());
    }

    public CustomerNumber create(Customer.Details customerDetails) {
        Create create = adapter.send(new Create(
                dateOrNull(customerDetails.dateOfBirth()), customerDetails.familyName(), customerDetails.firstName(),
                customerDetails.nationality(), 0L, customerDetails.title()));
        return new CustomerNumber(create.getNumber());
    }

    public Customer update(CustomerNumber customerNumber, Customer.Details customerDetails) {
        Update update = adapter.send(new Update(dateOrNull(customerDetails.dateOfBirth()),
                customerDetails.familyName(), customerDetails.firstName(), customerDetails.nationality(),
                customerNumber.value(), customerDetails.title()));
        return toCustomer(customerNumber, update.getTitle(), update.getFirstName(), update.getFamilyName(),
                update.getStatus(), update.getDateOfBirth(), update.getNationality());
    }

    private static String dateOrNull(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value);
    }

    private static Customer.Summary toSummary(Search.Item item) {
        try {
            return new Customer.Summary(new CustomerNumber(item.getNumber()), item.getTitle(),
                    item.getFirstName(), item.getFamilyName(), parseDate(item.getDateOfBirth()),
                    Customer.Status.fromCode(item.getStatus()));
        } catch (IllegalArgumentException | NullPointerException | DateTimeParseException e) {
            throw new MainframeException(
                    StatusMessage.UNABLE_TO_PARSE.getCode(), "The mainframe returned a customer record that could " +
                    "not be read.", e);
        }
    }

    private static Customer toCustomer(CustomerNumber number, String title, String firstName,
                                       String familyName, String status, String dateOfBirth, String nationality) {
        try {
            return new Customer(number, title, firstName, familyName,
                    Customer.Status.fromCode(status), parseDate(dateOfBirth), nationality);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new MainframeException(
                    StatusMessage.UNABLE_TO_PARSE.getCode(), "The mainframe returned a customer record that could " +
                    "not be read.", e);
        }
    }
}
