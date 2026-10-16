package lavs.logic.service;

import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.model.customer.CustomerNumber;

// Table 1: a customer needs at least one phone number or email address, so the last one cannot be deleted
final class ContactRequirement {

    static final String LAST_CONTACT_MESSAGE =
            "A customer needs at least one phone number or email address, so the last one cannot be deleted.";

    private final PhoneGateway phones;
    private final EmailGateway emails;

    ContactRequirement(PhoneGateway phones, EmailGateway emails) {
        this.phones = phones;
        this.emails = emails;
    }

    boolean allowsDeletingOne(CustomerNumber customer) {
        return contactCount(customer) > 1;
    }

    boolean hasAny(CustomerNumber customer) {
        return contactCount(customer) > 0;
    }

    private long contactCount(CustomerNumber customer) {
        return phones.count(customer) + emails.count(customer);
    }
}
