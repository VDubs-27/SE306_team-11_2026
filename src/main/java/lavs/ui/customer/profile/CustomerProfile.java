package lavs.ui.customer.profile;

import java.util.List;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.Email;
import lavs.logic.model.customer.Employer;
import lavs.logic.model.customer.Phone;
import lavs.logic.model.loan.Loan;

/**
 * Everything the profile screen shows for one customer.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
public record CustomerProfile(
        Customer customer,
        List<Address> addresses,
        List<Phone> phones,
        List<Email> emails,
        List<Employer> employers,
        List<Loan> loans,
        List<DocumentRow> documents,
        List<NoteRow> notes) {
}
