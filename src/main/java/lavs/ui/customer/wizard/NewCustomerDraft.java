package lavs.ui.customer.wizard;

import java.util.ArrayList;
import java.util.List;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;

/**
 * What has been entered in the new customer wizard so far. Each step reads and fills its part;
 * nothing reaches the mainframe until the customer is saved.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
final class NewCustomerDraft {

    private Customer.Details details;
    private final List<Address.Details> addresses = new ArrayList<>();
    private final List<Phone.Details> phones = new ArrayList<>();
    private final List<Email.Details> emails = new ArrayList<>();
    private final List<Employer.Details> employers = new ArrayList<>();
    private boolean employed = true;

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

    /** The phone numbers added so far. The Contacts step edits this list. */
    List<Phone.Details> phones() {
        return phones;
    }

    /** The email addresses added so far. The Contacts step edits this list. */
    List<Email.Details> emails() {
        return emails;
    }

    /** The employers added so far. The Employment step edits this list. */
    List<Employer.Details> employers() {
        return employers;
    }

    /** False when the customer isn't employed; their employer list is then ignored. */
    boolean isEmployed() {
        return employed;
    }

    void setEmployed(boolean employed) {
        this.employed = employed;
    }
}
