package lavs.ui.loan.people;

import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.service.Result;
import lavs.ui.common.Fetch;
import lavs.ui.customer.profile.ProfileLoader;
import lavs.ui.shell.CustomerServices;

/**
 * Reads what a loan's people list shows about someone: who they are, where they live and whether
 * they work. Run it in the background.
 * AI-assisted: written by Jack, refined with Claude.
 */
public final class PersonLoader {

    private PersonLoader() {
    }

    /** The customer with this number as a person on a loan in this role (null for the applicant). */
    public static Result<LoanPerson> load(CustomerServices services, CustomerNumber number, Coborrower.Type role) {
        return Fetch.run(() -> {
            String suburb = Fetch.value(ProfileLoader.primarySuburb(services, number));
            return new LoanPerson(Fetch.value(services.customers().retrieve(number)), role,
                    suburb.isEmpty() ? null : suburb,
                    Fetch.value(services.employers().list(number, 1, 1)).total() > 0);
        });
    }
}
