package lavs.ui.loan.wizard;

import static lavs.logic.model.Validation.isBlank;

import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.loan.Coborrower;
import lavs.ui.customer.profile.CustomerProfile;
import lavs.ui.customer.profile.SampleProfiles;

/**
 * Someone on a new loan: the applicant (role null), or a co-borrower or guarantor, with what the
 * Co-borrowers step shows about them.
 * AI-assisted: written with Claude from the team's mockups; reviewed by Jack Preston.
 */
record LoanPerson(Customer customer, Coborrower.Type role, String suburb, boolean employed) {

    /** Looks up where the customer lives and whether they work, to show beside their name. */
    static LoanPerson lookUp(Customer customer, Coborrower.Type role) {
        // Later: read these from CustomerService instead of the sample profiles
        CustomerProfile profile = SampleProfiles.find(customer.number().value());
        String suburb = profile.addresses().stream()
                .filter(Address::isPrimary)
                .map(Address::suburb)
                .filter(place -> !isBlank(place))
                .findFirst()
                .orElse(null);
        return new LoanPerson(customer, role, suburb, !profile.employers().isEmpty());
    }

    /** True for the customer the loan is for. */
    boolean isApplicant() {
        return role == null;
    }
}
