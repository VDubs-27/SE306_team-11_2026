package lavs.ui.customer.wizard;

import java.util.ArrayList;
import java.util.List;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;

/**
 * What has been entered in the new customer wizard so far. Each step reads and fills its part;
 * nothing reaches the mainframe until the customer is saved.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class NewCustomerDraft {

    private Customer.Details details;
    private final List<Address.Details> addresses = new ArrayList<>();

    /** The customer's personal details, once the Details step has passed its check; null before. */
    Customer.Details details() {
        return details;
    }

    void setDetails(Customer.Details details) {
        this.details = details;
    }

    /** The addresses added so far, in the order they were added. The Addresses step edits this list. */
    List<Address.Details> addresses() {
        return addresses;
    }
}
