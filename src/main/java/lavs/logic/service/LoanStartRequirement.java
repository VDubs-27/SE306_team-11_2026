package lavs.logic.service;

import lavs.logic.adapter.AddressGateway;
import lavs.logic.adapter.CustomerGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.customer.Address;
import lavs.logic.model.customer.Customer;
import lavs.logic.model.customer.CustomerNumber;

import java.util.ArrayList;
import java.util.List;

final class LoanStartRequirement {

    static final FieldError NOT_ACTIVE =
            new FieldError("status", "Only an active customer can have a new loan. Make the customer active first.");
    static final FieldError NO_MAILING_ADDRESS =
            new FieldError("mailingAddress", "The customer needs a mailing address before a loan can be started.");
    static final FieldError NO_CONTACT =
            new FieldError("contacts", "The customer needs a phone number or email address before a loan can be started.");

    private final CustomerGateway customerGateway;
    private final AddressGateway addressGateway;
    private final ContactRequirement contactRequirement;

    LoanStartRequirement(CustomerGateway customerGateway, AddressGateway addressGateway, ContactRequirement contactRequirement) {
        this.customerGateway = customerGateway;
        this.addressGateway = addressGateway;
        this.contactRequirement = contactRequirement;
    }

    List<FieldError> problems(CustomerNumber customerNumber) {
        if (customerGateway.retrieve(customerNumber).status() != Customer.Status.ACTIVE) {
            return List.of(NOT_ACTIVE);
        }
        List<FieldError> errors = new ArrayList<>();
        if (addressGateway.all(customerNumber).stream().noneMatch(Address.Summary::isMailing)) {
            errors.add(NO_MAILING_ADDRESS);
        }
        if (!contactRequirement.hasAny(customerNumber)) {
            errors.add(NO_CONTACT);
        }
        return errors;
    }
}
