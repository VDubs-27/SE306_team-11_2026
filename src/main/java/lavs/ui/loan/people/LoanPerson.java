package lavs.ui.loan.people;

import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;

/**
 * Someone on a loan: the applicant (role null), or a co-borrower or guarantor, with what the
 * Co-borrowers step and the loan screen show about them.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record LoanPerson(Customer customer, Coborrower.Type role, String suburb, boolean employed) {

    /** True for the customer the loan is for. */
    public boolean isApplicant() {
        return role == null;
    }
}
