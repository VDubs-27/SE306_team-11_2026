package lavs.ui.customer.wizard;

import lavs.logic.model.customer.Customer;

/**
 * What has been entered in the new customer wizard so far. Each step reads and fills its part;
 * nothing reaches the mainframe until the customer is saved.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class NewCustomerDraft {

    private Customer.Details details;

    /** The customer's personal details, once the Details step has passed its check; null before. */
    Customer.Details details() {
        return details;
    }

    void setDetails(Customer.Details details) {
        this.details = details;
    }
}
