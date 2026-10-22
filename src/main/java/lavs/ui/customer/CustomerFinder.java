package lavs.ui.customer;

import java.util.List;
import java.util.regex.Pattern;

import lavs.logic.model.Page;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.service.CustomerService;
import lavs.logic.service.Result;

/**
 * Finds customers by name or customer number. The mainframe's search only matches names, so a full
 * customer number, e.g. 01-0001-0000015, is looked up directly instead and found as the only result.
 * Run it in the background.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class CustomerFinder {

    /** A whole customer number: bank, branch and account, e.g. 01-0001-0000015. */
    private static final Pattern CUSTOMER_NUMBER = Pattern.compile("\\d{2}-\\d{4}-\\d{7}");

    private CustomerFinder() {
    }

    /** A page of the customers whose name matches the term, or the one customer with that number. */
    public static Result<Page<Customer.Summary>> find(CustomerService customers, String term, long page) {
        String trimmed = term.trim();
        if (!CUSTOMER_NUMBER.matcher(trimmed).matches()) {
            return customers.search(trimmed, page);
        }
        Result<Customer> found = customers.retrieve(new CustomerNumber(trimmed));
        if (!found.successful()) {
            // No customer has that number, or the mainframe couldn't be reached: the search says which
            return customers.search(trimmed, page);
        }
        Customer customer = found.value();
        return Result.success(new Page<>(List.of(new Customer.Summary(customer.number(), customer.title(),
                customer.firstName(), customer.familyName(), customer.dateOfBirth(), customer.status())), 1, 1, 1));
    }
}
