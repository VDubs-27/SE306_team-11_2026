package lavs.logic.service;

import java.util.ArrayList;
import java.util.List;
import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CoborrowerGateway;
import lavs.logic.adapter.EmployerGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.customer.Address;
import lavs.logic.model.loan.Coborrower;
import lavs.logic.model.loan.Loan;

// Table 1: only a New loan can move to Pending, and only once the customer has a mailing address and,
// if they have no employer, the loan has a guarantor. The loan service checks this before Loan.withStatus.
final class SubmitRequirement {

    static final FieldError NOT_NEW = new FieldError("status", "Only a new loan can be submitted.");
    static final FieldError NO_MAILING_ADDRESS =
            new FieldError("mailingAddress", "The customer needs a mailing address before the loan can be submitted.");
    static final FieldError NO_GUARANTOR =
            new FieldError("guarantor", "The customer has no employer, so the loan needs a guarantor.");

    private final AddressGateway addresses;
    private final EmployerGateway employers;
    private final CoborrowerGateway coborrowers;

    SubmitRequirement(AddressGateway addresses, EmployerGateway employers, CoborrowerGateway coborrowers) {
        this.addresses = addresses;
        this.employers = employers;
        this.coborrowers = coborrowers;
    }

    List<FieldError> problems(Loan loan) {
        if (!loan.status().canMoveTo(Loan.Status.PENDING)) {
            return List.of(NOT_NEW);
        }
        List<FieldError> errors = new ArrayList<>();
        if (addresses.all(loan.customerNumber()).stream().noneMatch(Address.Summary::isMailing)) {
            errors.add(NO_MAILING_ADDRESS);
        }
        if (employers.count(loan.customerNumber()) == 0 && !hasGuarantor(loan)) {
            errors.add(NO_GUARANTOR);
        }
        return errors;
    }

    private boolean hasGuarantor(Loan loan) {
        return coborrowers.all(loan.loanNumber()).stream()
                .anyMatch(person -> person.type() == Coborrower.Type.GUARANTOR);
    }
}
