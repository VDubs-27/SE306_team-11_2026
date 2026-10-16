package lavs.ui.customer.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.service.Result;
import lavs.ui.common.AddressRows;
import lavs.ui.common.ContactRows;
import lavs.ui.common.Fetch;
import lavs.ui.shell.CustomerServices;

/**
 * Saves what the new customer wizard collected: creates the customer, then adds their addresses,
 * contacts and employers one by one. Once the customer exists it is never created again, so if a
 * record after that fails, the rest still save and the ones that didn't are listed, to add from the
 * profile. Run it in the background.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class CustomerSaver {

    private CustomerSaver() {
    }

    /** A customer just created, and what couldn't be added for them, e.g. "Email a@b.com: …"; empty if all saved. */
    record Saved(Customer customer, List<String> notSaved) {
    }

    /** The customer as created, or why they couldn't be; nothing is saved when creating fails. */
    static Result<Saved> save(CustomerServices services, NewCustomerDraft draft) {
        Result<CustomerNumber> created = services.customers().create(draft.details());
        if (!created.successful()) {
            return new Result<>(null, created.fieldErrors(), created.errorMessage());
        }
        CustomerNumber number = created.value();
        List<String> notSaved = new ArrayList<>();
        addEach(draft.addresses(), address -> services.addresses().add(number, address),
                address -> "Address " + AddressRows.street(address), notSaved);
        addEach(draft.phones(), phone -> services.phones().add(number, phone),
                phone -> "Phone " + ContactRows.number(phone), notSaved);
        addEach(draft.emails(), email -> services.emails().add(number, email),
                email -> "Email " + email.address(), notSaved);
        if (draft.isEmployed()) {
            addEach(draft.employers(), employer -> services.employers().add(number, employer),
                    employer -> "Employer " + employer.name(), notSaved);
        }
        return Result.success(new Saved(customer(services, number, draft.details()), notSaved));
    }

    /** Adds each record, noting any that fail with what it was and why, e.g. "Phone +64 21 555 0182: …". */
    private static <D> void addEach(List<D> records, Function<D, Result<?>> add, Function<D, String> name,
                                    List<String> notSaved) {
        for (D record : records) {
            Result<?> added = add.apply(record);
            if (!added.successful()) {
                notSaved.add(name.apply(record) + ": " + Fetch.reason(added));
            }
        }
    }

    /** The customer as the mainframe now has them; as entered, if reading them back fails. */
    private static Customer customer(CustomerServices services, CustomerNumber number, Customer.Details details) {
        Result<Customer> read = services.customers().retrieve(number);
        return read.successful() ? read.value() : new Customer(number, details.title(), details.firstName(),
                details.familyName(), Customer.Status.ACTIVE, details.dateOfBirth(), details.nationality());
    }
}
