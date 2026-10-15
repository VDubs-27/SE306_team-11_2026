package lavs.logic.service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lavs.logic.adapter.EmailGateway;
import lavs.logic.adapter.PhoneGateway;
import lavs.logic.model.FieldError;
import lavs.logic.model.Page;
import lavs.logic.model.constants.ConstantCategory;
import lavs.logic.model.constants.Constants;
import lavs.logic.model.customer.CustomerNumber;
import lavs.logic.model.customer.Phone;

public class PhoneService {

    private final PhoneGateway phones;
    private final ContactRequirement contacts;
    private final Supplier<Constants> constants;

    public PhoneService(PhoneGateway phones, EmailGateway emails, Supplier<Constants> constants) {
        this.phones = phones;
        this.contacts = new ContactRequirement(
                Objects.requireNonNull(phones, "phones"), Objects.requireNonNull(emails, "emails"));
        this.constants = Objects.requireNonNull(constants, "constants");
    }

    public Result<Page<Phone.Summary>> list(CustomerNumber customer, long page, long size) {
        return Result.attempt(() -> Result.success(phones.list(customer, page, size)));
    }

    public Result<Phone> get(CustomerNumber customer, long id) {
        return Result.attempt(() -> Result.success(phones.retrieve(customer, id)));
    }

    public Result<Phone> add(CustomerNumber customer, Phone.Details details) {
        return save(customer, Rules.NEW_RECORD, details, () -> phones.add(customer, details));
    }

    public Result<Phone> update(CustomerNumber customer, long id, Phone.Details details) {
        return save(customer, id, details, () -> phones.update(customer, id, details));
    }

    public Result<Void> delete(CustomerNumber customer, long id) {
        return Result.attempt(() -> {
            if (!contacts.allowsDeletingOne(customer)) {
                return Result.failed(ContactRequirement.LAST_CONTACT_MESSAGE);
            }
            phones.delete(customer, id);
            return Result.success(null);
        });
    }

    private Result<Phone> save(CustomerNumber customer, long id, Phone.Details details, Supplier<Phone> send) {
        List<FieldError> errors = Rules.withTypeCheck(
                details.validate(), constants.get(), ConstantCategory.PHONE_TYPE, details.type(), "Phone type");
        if (!errors.isEmpty()) {
            return Result.invalid(errors);
        }
        return Result.attempt(() -> {
            if (details.isPrimary() && Rules.otherIsPrimary(
                    phones.all(customer), id, Phone.Summary::isPrimary, Phone.Summary::id)) {
                return Result.invalid(List.of(Rules.primaryTaken("phone number")));
            }
            return Result.success(send.get());
        });
    }
}
