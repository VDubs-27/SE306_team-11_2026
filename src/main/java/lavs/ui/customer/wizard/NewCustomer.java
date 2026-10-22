package lavs.ui.customer.wizard;

import lavs.logic.model.customer.Customer;

/**
 * A customer just saved by the new customer wizard, with what a screen that asked for them may
 * want to show straight away: where they live and whether they work (e.g. for the loan wizard's
 * people list and its guarantor rule).
 * AI-assisted: written by Jack, refined with Claude.
 */
public record NewCustomer(Customer customer, String suburb, boolean employed) {
}
