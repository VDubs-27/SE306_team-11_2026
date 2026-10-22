package lavs.ui.customer.profile;

import static lavs.logic.model.Validation.isBlank;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.service.Result;
import lavs.ui.common.Fetch;
import lavs.ui.shell.CustomerServices;
import lavs.ui.shell.Services;

/**
 * Reads what the profile screen needs before it opens: the customer, their primary address's suburb,
 * and how many records each tab has. Kept to a few calls, as some customers have hundreds of records;
 * each tab reads its own page of records when it's opened. Run it in the background.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class ProfileLoader {

    private ProfileLoader() {
    }

    /** The profile of the customer with this number, or why it couldn't be read. */
    public static Result<CustomerProfile> load(Services all, String customerNumber) {
        CustomerServices services = all.customer();
        CustomerNumber number = new CustomerNumber(customerNumber);
        return Fetch.run(() -> new CustomerProfile(
                Fetch.value(services.customers().retrieve(number)),
                Fetch.value(primarySuburb(services, number)),
                count(services.addresses().list(number, 1, 1)),
                count(services.phones().list(number, 1, 1)) + count(services.emails().list(number, 1, 1)),
                count(services.employers().list(number, 1, 1)),
                count(all.loan().loans().listForCustomer(number, 1, 1)),
                count(services.documents().list(number, 1, 1)),
                Fetch.value(services.notes().count(number))));
    }

    /** The suburb of the customer's primary address, or "" when there isn't one. Run it in the background. */
    public static Result<String> primarySuburb(CustomerServices services, CustomerNumber number) {
        return Fetch.run(() -> Fetch.all(page -> services.addresses().list(number, page, Fetch.PAGE_SIZE)).stream()
                .filter(Address.Summary::isPrimary)
                .findFirst()
                .map(primary -> Fetch.value(services.addresses().get(number, primary.id())).suburb())
                .filter(suburb -> !isBlank(suburb))
                .orElse(""));
    }

    private static long count(Result<? extends Page<?>> firstPage) {
        return Fetch.value(firstPage).total();
    }
}
