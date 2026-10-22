package lavs.ui.customer.profile;

import lavs.logic.model.customer.Customer;
import lavs.ui.loan.people.LoanPerson;

/**
 * What the profile screen needs before it opens: the customer, the suburb of their primary address
 * for the card at the top ("" when there isn't one), and how many of each record they have, for the
 * tabs' counts. Each tab reads its own records when it's first opened.
 * AI-assisted: written by Jack, refined with Claude.
 */
public record CustomerProfile(
        Customer customer,
        String primarySuburb,
        long addresses,
        long contacts,
        long employers,
        long loans,
        long documents,
        long notes) {

    /** The same profile with the primary address's suburb as it is now, after the addresses changed. */
    public CustomerProfile withPrimarySuburb(String suburb) {
        return new CustomerProfile(customer, suburb, addresses, contacts, employers, loans, documents, notes);
    }

    /** The same profile with this many employers, after the employers changed. */
    public CustomerProfile withEmployers(long count) {
        return new CustomerProfile(customer, primarySuburb, addresses, contacts, count, loans, documents, notes);
    }

    /**
     * The customer as the person a loan is for, with where they live and whether they work.
     * customerNow is the customer as they are now, as their details may have been edited since loading.
     */
    public LoanPerson asApplicant(Customer customerNow) {
        return new LoanPerson(customerNow, null, primarySuburb.isEmpty() ? null : primarySuburb, employers > 0);
    }
}
